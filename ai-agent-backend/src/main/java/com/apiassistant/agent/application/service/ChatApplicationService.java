package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.port.out.AgentSessionRepositoryPort;
import com.apiassistant.agent.application.port.out.ChatStrategyRegistryPort;
import com.apiassistant.agent.application.port.out.PlaybookRepositoryPort;
import com.apiassistant.agent.application.port.out.SessionToolProviderPort;
import com.apiassistant.agent.application.strategy.ChatStrategy;
import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.agentsession.aggregate.vo.SessionId;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 處理與 AI 代理對話的核心應用服務 (Application Service)。
 * <p>
 * 負責管理對話記憶體 (Chat Memory)、組裝系統提示 (System Prompt)，
 * 以及依據 Session ID 動態載入可用的工具 (Tools/MCP)，確保每個 Session 只能使用被授權的工具。
 * </p>
 */
@Slf4j
@Service
public class ChatApplicationService {

    private static final String SYSTEM_PROMPT = """
            你是一個專業的 API 助手。請遵守以下規範：
            1. 使用繁體中文回答，保持簡潔。
            2. 當需要呼叫 API 時，你必須使用 Tool Calling 機制（function calling），絕對不要把工具呼叫寫成 JSON 文字輸出。
            3. 回覆只用自然語言，禁止輸出 JSON 或工具語法。
            """;

    private static final int MAX_TOOL_CALL_RETRIES = 1;
    private static final String CHAT_MEMORY_CONVERSATION_ID = "chat_memory_conversation_id";

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final List<ToolCallbackProvider> toolProviders;
    private final AgentSessionRepositoryPort agentSessionRepositoryPort;
    private final PlaybookRepositoryPort playbookRepositoryPort;
    private final SessionToolProviderPort sessionToolProviderPort;
    private final ChatStrategyRegistryPort strategyRegistry;

    /**
     * 建構子，初始化 ChatClient 與對話記憶體，並載入系統所有的 ToolCallbackProviders 與 ChatStrategies。
     *
     * @param chatClientBuilder Spring AI 的 ChatClient Builder，用於建立 ChatClient
     * @param toolProviders     系統中所有註冊的工具提供者 (包含 MCP Tools 等)
     * @param strategyRegistry  負責提供 ChatStrategy 的 Registry
     */
    public ChatApplicationService(ChatClient.Builder chatClientBuilder, List<ToolCallbackProvider> toolProviders, AgentSessionRepositoryPort agentSessionRepositoryPort, PlaybookRepositoryPort playbookRepositoryPort, SessionToolProviderPort sessionToolProviderPort, ChatStrategyRegistryPort strategyRegistry) {
        this.agentSessionRepositoryPort = agentSessionRepositoryPort;
        this.playbookRepositoryPort = playbookRepositoryPort;
        this.sessionToolProviderPort = sessionToolProviderPort;
        this.strategyRegistry = strategyRegistry;

        // Initialize chat memory store
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();

        ChatClient.Builder builder = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build());

        this.toolProviders = toolProviders != null ? toolProviders : new java.util.ArrayList<>();

        this.chatClient = builder.build();
    }

    /**
     * 取得特定 Session 的對話歷史紀錄。
     *
     * @param sessionId 使用者的對話 Session ID
     * @return 該 Session 過去的對話訊息列表
     */
    public List<org.springframework.ai.chat.messages.Message> getHistory(String sessionId) {
        return chatMemory.get(sessionId);
    }

    /**
     * 清除特定 Session 的對話歷史紀錄。
     * 當 Session 重新綁定劇本時，需呼叫此方法清除舊有的對話，避免 AI 被舊有的歷史紀錄影響。
     *
     * @param sessionId 使用者的對話 Session ID
     */
    public void clearHistory(String sessionId) {
        chatMemory.clear(sessionId);
    }

    /**
     * 處理使用者的對話請求，將訊息傳送給 AI 模型並回傳結果。
     *
     * @param sessionId   使用者的對話 Session ID
     * @param userMessage 使用者輸入的新對話訊息
     * @return AI 產生的回覆字串；若發生錯誤則回傳錯誤提示
     */
    public String chat(String sessionId, String userMessage) {
        log.info("Processing chat for session {}: {}", sessionId, userMessage);

        try {
            ChatRequestData data = prepareChatRequest(sessionId);
            ChatStrategy selectedStrategy = selectStrategy(data.session(), data.playbook());

            if (selectedStrategy != null && data.session() != null) {
                ChatStrategy.PreProcessResult result = selectedStrategy.preProcess(sessionId, userMessage, data.session(), data.playbook());

                userMessage = handlePreProcessResult(sessionId, userMessage, result, data.session());
            }

            String systemPromptText = SYSTEM_PROMPT;
            if (selectedStrategy != null) {
                systemPromptText = selectedStrategy.getSystemPrompt(SYSTEM_PROMPT, data.session(), data.playbook(), data.allowedToolNames(), sessionId);
            }

            var prompt = chatClient.prompt()
                    .user(userMessage)
                    .system(systemPromptText)
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID, sessionId));

            java.util.List<org.springframework.ai.tool.ToolCallback> activeCallbacks = getActiveCallbacks(selectedStrategy, data);

            if (!activeCallbacks.isEmpty()) {
                prompt = prompt.toolCallbacks(activeCallbacks.toArray(new org.springframework.ai.tool.ToolCallback[0]));
            }

            String result = prompt.call().content();
            return retryFailedToolCallIfNeeded(result, sessionId, activeCallbacks, systemPromptText);
        } catch (Exception e) {
            log.error("Error during chat prompt", e);
            return "很抱歉，AI 系統發生錯誤：" + e.getMessage();
        }
    }

    /**
     * 處理使用者的對話請求，將訊息傳送給 AI 模型並以串流 (Streaming) 方式回傳結果。
     * &lt;p&gt;
     * 注意：當有 Tool Callbacks 時，會自動降級為非串流模式 (.call())，
     * 因為 Ollama/Qwen2.5 在串流模式下無法正確處理 Tool Calling。
     * &lt;/p&gt;
     *
     * @param sessionId   使用者的對話 Session ID
     * @param userMessage 使用者輸入的新對話訊息
     * @return AI 產生的回覆字串串流 (Flux)；若發生錯誤則回傳包含錯誤提示的串流
     */
    public reactor.core.publisher.Flux<String> chatStream(String sessionId, String userMessage) {
        log.info("Processing chat stream for session {}: {}", sessionId, userMessage);

        try {
            ChatRequestData data = prepareChatRequest(sessionId);
            ChatStrategy selectedStrategy = selectStrategy(data.session(), data.playbook());

            if (selectedStrategy != null && data.session() != null) {
                ChatStrategy.PreProcessResult result = selectedStrategy.preProcess(sessionId, userMessage, data.session(), data.playbook());

                userMessage = handlePreProcessResult(sessionId, userMessage, result, data.session());
            }

            String systemPromptText = SYSTEM_PROMPT;
            if (selectedStrategy != null) {
                systemPromptText = selectedStrategy.getSystemPrompt(SYSTEM_PROMPT, data.session(), data.playbook(), data.allowedToolNames(), sessionId);
            }

            var prompt = chatClient.prompt()
                    .user(userMessage)
                    .system(systemPromptText)
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID, sessionId));

            java.util.List<org.springframework.ai.tool.ToolCallback> activeCallbacks = getActiveCallbacks(selectedStrategy, data);

            if (!activeCallbacks.isEmpty()) {
                // 【關鍵】當有 Tool Callbacks 時，改用非串流模式 (.call())。
                // Ollama/Qwen2.5 在串流模式下無法正確處理 Tool Calling，
                // 會把 Tool Call 當成純文字輸出，導致無限生成 Token。
                log.info("Tools active for session {}, falling back to non-streaming mode. Active tools: {}",
                        sessionId, activeCallbacks.stream().map(c -> c.getToolDefinition().name()).toList());
                prompt = prompt.toolCallbacks(activeCallbacks.toArray(new org.springframework.ai.tool.ToolCallback[0]));

                try {
                    String result = prompt.call().content();
                    if (result == null || result.isBlank()) {
                        log.warn("AI returned empty response for session {} with tools active", sessionId);
                        return reactor.core.publisher.Flux.just("工具已呼叫完成，但 AI 未回傳摘要訊息。請輸入「繼續」來進行下一步。");
                    }

                    result = retryFailedToolCallIfNeeded(result, sessionId, activeCallbacks, systemPromptText);
                    return reactor.core.publisher.Flux.just(result);
                } catch (Exception toolCallError) {
                    log.error("Error during tool-calling chat for session {}", sessionId, toolCallError);
                    return reactor.core.publisher.Flux.just("工具呼叫失敗：" + toolCallError.getMessage());
                }
            }

            // 無工具時使用串流模式
            return prompt.stream().content();
        } catch (Exception e) {
            log.error("Error during chat stream prompt", e);
            return reactor.core.publisher.Flux.just("很抱歉，AI 系統發生錯誤：" + e.getMessage());
        }
    }

    /**
     * 若 AI 回傳的結果疑似為純文字格式的工具呼叫（而非標準的 Function Calling 格式），
     * 則進行重試邏輯，提示 AI 改用標準格式輸出。
     *
     * @param initialResult    AI 首次回傳的原始字串結果
     * @param sessionId        當前對話的 Session ID
     * @param activeCallbacks  當前允許呼叫的工具列表
     * @param systemPromptText 系統提示詞
     * @return 最終的結果字串（若重試成功則為新的結果，否則為最後一次重試的結果或原始結果）
     */
    private String retryFailedToolCallIfNeeded(String initialResult, String sessionId, java.util.List<org.springframework.ai.tool.ToolCallback> activeCallbacks, String systemPromptText) {
        if (activeCallbacks.isEmpty()) {
            return initialResult;
        }

        String result = initialResult;
        int retries = 0;
        
        while (looksLikeFailedToolCall(result) && retries < MAX_TOOL_CALL_RETRIES) {
            retries++;
            log.warn("Detected failed tool call in AI response for session {}. Retrying (Attempt {}/{})... Response: {}", 
                    sessionId, retries, MAX_TOOL_CALL_RETRIES, result != null ? result.substring(0, Math.min(200, result.length())) : "null");

            String retryMessage = "你剛才把工具呼叫輸出為純文字了，這是錯誤的。請使用 Tool Calling 機制（function calling）來呼叫工具，不要輸出 JSON 文字。請立刻重新嘗試呼叫工具。";

            var retryPrompt = chatClient.prompt()
                    .user(retryMessage)
                    .system(systemPromptText)
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID, sessionId))
                    .toolCallbacks(activeCallbacks.toArray(new org.springframework.ai.tool.ToolCallback[0]));

            try {
                String retryResult = retryPrompt.call().content();
                result = retryResult;
                if (retryResult != null && !retryResult.isBlank() && !looksLikeFailedToolCall(retryResult)) {
                    log.info("Retry {} succeeded for session {}", retries, sessionId);
                    return retryResult;
                }
            } catch (Exception retryError) {
                log.warn("Retry {} failed for session {}", retries, sessionId, retryError);
            }

            if (retries >= MAX_TOOL_CALL_RETRIES) {
                log.warn("Max retries ({}) reached. Still produced failed tool call for session {}", MAX_TOOL_CALL_RETRIES, sessionId);
            }
        }

        return result;
    }

    /**
     * 根據當前的對話策略與允許的工具名單，過濾並取得實際可用的工具 Callback 列表。
     * 同時確保系統必備工具（如 advancePlaybookStep）始終包含在內。
     *
     * @param selectedStrategy 目前套用的對話策略
     * @param data             包含 Session 狀態與允許工具名單的請求資料
     * @return 允許在此次對話中被 AI 呼叫的工具列表
     */
    private java.util.List<org.springframework.ai.tool.ToolCallback> getActiveCallbacks(ChatStrategy selectedStrategy, ChatRequestData data) {
        java.util.List<org.springframework.ai.tool.ToolCallback> currentAllCallbacks = new java.util.ArrayList<>();
        for (ToolCallbackProvider provider : toolProviders) {
            currentAllCallbacks.addAll(java.util.Arrays.asList(provider.getToolCallbacks()));
        }

        java.util.List<org.springframework.ai.tool.ToolCallback> activeCallbacks = new java.util.ArrayList<>();
        if (selectedStrategy != null) {
            activeCallbacks = selectedStrategy.getActiveTools(data.session(), data.playbook(), currentAllCallbacks, data.allowedToolNames());
        }

        // 系統必備工具：必定允許 AI 呼叫 advancePlaybookStep
        for (org.springframework.ai.tool.ToolCallback callback : currentAllCallbacks) {
            if ("advancePlaybookStep".equals(callback.getToolDefinition().name())) {
                if (activeCallbacks.stream().noneMatch(c -> c.getToolDefinition().name().equals("advancePlaybookStep"))) {
                    activeCallbacks.add(callback);
                }
                break;
            }
        }
        return activeCallbacks;
    }

    /**
     * 處理策略前置作業 (preProcess) 執行後的狀態結果。
     * 負責儲存 Session 狀態、清除對話記憶體（若需要），並視情況組合歷史工具執行結果作為上下文附加到使用者的訊息中。
     *
     * @param sessionId   當前對話的 Session ID
     * @param userMessage 使用者原始輸入的對話訊息
     * @param result      策略前置作業回傳的處理結果
     * @param session     當前的對話 Session
     * @return 處理過後、準備送給 AI 模型的最終使用者訊息
     */
    private String handlePreProcessResult(String sessionId, String userMessage, ChatStrategy.PreProcessResult result, AgentSession session) {
        if (result == ChatStrategy.PreProcessResult.SAVE_SESSION || result == ChatStrategy.PreProcessResult.SAVE_AND_CLEAR) {
            agentSessionRepositoryPort.save(session);
        }

        if (result == ChatStrategy.PreProcessResult.SAVE_AND_CLEAR) {
            String contextText = extractToolContext(sessionId);
            chatMemory.clear(sessionId);

            if (session.getPlaybookId() != null) {
                return "好的，請繼續執行當前步驟。若具備所有參數請直接呼叫工具，若缺少參數請向我詢問。" + contextText;
            }
        } else if (result == ChatStrategy.PreProcessResult.SAVE_SESSION && session.getPlaybookId() != null) {
            return "好的，請繼續執行當前步驟。若具備所有參數請直接呼叫工具，若缺少參數請向我詢問。";
        }

        return userMessage;
    }

    /**
     * 從對話歷史紀錄中提取工具執行的結果（Tool Messages），將其組合為純文字上下文。
     * 這通常在切換步驟或需要提供歷史參數給 AI 參考時使用。
     *
     * @param sessionId 當前對話的 Session ID
     * @return 包含歷史工具執行結果的格式化字串；若無資料則回傳空字串
     */
    private String extractToolContext(String sessionId) {
        try {
            var messages = chatMemory.get(sessionId);
            StringBuilder sb = new StringBuilder();
            for (var msg : messages) {
                if (msg.getMessageType() == org.springframework.ai.chat.messages.MessageType.TOOL) {
                    sb.append("- ").append(msg).append("\n");
                }
            }
            if (!sb.isEmpty()) {
                return "\n\n【歷史工具執行結果】（請從這裡提取 Token 或其他所需參數）：\n" + sb;
            }
        } catch (Exception e) {
            log.warn("Failed to extract context", e);
        }
        return "";
    }

    /**
     * 準備此次對話請求所需的相關資料。
     * 使用非同步並發 (CompletableFuture) 取得允許的工具清單與 Session 狀態，並查出對應的 Playbook。
     *
     * @param sessionId 當前對話的 Session ID
     * @return 封裝了工具清單、Session 與 Playbook 的資料物件
     */
    private ChatRequestData prepareChatRequest(String sessionId) {
        java.util.concurrent.CompletableFuture<java.util.List<String>> toolsFuture = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                sessionToolProviderPort.getSessionToolNames(sessionId)
        );

        java.util.concurrent.CompletableFuture<AgentSession> sessionFuture = java.util.concurrent.CompletableFuture.supplyAsync(() ->
                agentSessionRepositoryPort.findById(SessionId.of(sessionId)).orElse(null)
        );

        java.util.List<String> allowedToolNames = toolsFuture.join();
        AgentSession session = sessionFuture.join();

        Playbook playbook = null;
        if (session != null && session.getPlaybookId() != null) {
            playbook = playbookRepositoryPort.findById(session.getPlaybookId()).orElse(null);
        }

        return new ChatRequestData(allowedToolNames, session, playbook);
    }

    /**
     * 【策略自動選擇邏輯】
     * 透過 ChatStrategyRegistryPort 來取得對應的對話策略。
     */
    private ChatStrategy selectStrategy(AgentSession session, Playbook playbook) {
        return strategyRegistry.getStrategy(session, playbook);
    }

    /**
     * 取得當前 Session 所使用的 Agent 模式。
     *
     * @param sessionId Session ID
     * @return "PLAYBOOK" 或 "GENERAL"
     */
    public String getSessionAgentMode(String sessionId) {
        ChatRequestData data = prepareChatRequest(sessionId);
        ChatStrategy strategy = selectStrategy(data.session(), data.playbook());
        if (strategy != null && strategy.getClass().getSimpleName().equals("PlaybookAgentStrategy")) {
            return "PLAYBOOK";
        }
        return "GENERAL";
    }

    /**
     * 封裝對話請求所需資料的不可變紀錄類別 (Record)。
     *
     * @param allowedToolNames 當前 Session 允許使用的工具名稱清單
     * @param session          當前的對話 Session 實體
     * @param playbook         當前 Session 綁定的劇本（若無則為 null）
     */
    private record ChatRequestData(java.util.List<String> allowedToolNames, AgentSession session, Playbook playbook) {
    }

    /**
     * 偵測 AI 是否輸出了「偽工具呼叫」的純文字。
     * Qwen2.5 常見的失敗模式包含：
     * 1. 輸出 `portun {"name": "xxx", "arguments": {...}}`
     * 2. 輸出 `function_call` / `tool_call` 等關鍵字 + JSON
     * 3. 直接輸出包含 "name" 和 "arguments" 的 JSON 物件
     */
    private boolean looksLikeFailedToolCall(String response) {
        if (response == null || response.length() < 10) {
            return false;
        }
        String trimmed = response.trim();

        // Pattern 1: Contains JSON-like tool call structure
        if (trimmed.contains("\"name\"") && trimmed.contains("\"arguments\"") && trimmed.contains("{")) {
            return true;
        }

        // Pattern 2: Common Qwen2.5 garbled tool call prefixes
        if (trimmed.matches("(?s).*(portun|functcall|tool_call|function_call|<tool_call>|<function_call>)\\s*\\{.*}.*")) {
            return true;
        }

        // Pattern 3: Starts with raw JSON that looks like a tool invocation
        return trimmed.startsWith("{") && trimmed.contains("\"name\"");
    }
}
