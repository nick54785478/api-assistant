package com.apiassistant.agent.presentation.websocket;

import com.apiassistant.agent.application.command.CreateAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import com.apiassistant.agent.application.port.in.CreateAgentSessionUseCase;
import com.apiassistant.agent.application.service.ChatApplicationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Inbound Adapter for handling WebSocket Chat Messages.
 * Delegates to ChatApplicationService for LLM interactions.
 * 負責處理 WebSocket 聊天訊息的 Inbound Adapter (介面卡)。
 * 將使用者的對話委派給 ChatApplicationService 進行處理，並將大語言模型的串流回應 (Stream) 推送回客戶端。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatApplicationService chatService;
    private final CreateAgentSessionUseCase createAgentSessionUseCase;
    private final com.apiassistant.agent.application.port.in.BindPlaybookToSessionUseCase bindPlaybookToSessionUseCase;
    private final ObjectMapper objectMapper;

    /**
     * Invoked after WebSocket negotiation has succeeded and the WebSocket connection is opened.
     * 在 WebSocket 連線建立成功後觸發。
     * 這裡會向客戶端發送一則歡迎訊息，表示 AI 已準備就緒。
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        ObjectNode response = objectMapper.createObjectNode();
        response.put("role", "ai");
        response.put("content", "連線成功！Ollama 已準備就緒。");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Invoked when a new WebSocket text message arrives.
     * 處理從客戶端傳來的 WebSocket 文字訊息。
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message: {}", payload);
        
        String sessionId = null;
        String content = payload;
        String username = "Guest";
        String playbookId = null;
        
        // 嘗試將 Payload 解析為 JSON。如果失敗，則將整個 Payload 視為一般文字訊息 (Plain text)。
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            if (jsonNode.has("sessionId") && !jsonNode.get("sessionId").isNull() && !jsonNode.get("sessionId").asText().isBlank()) {
                sessionId = jsonNode.get("sessionId").asText();
            }
            if (jsonNode.has("content")) {
                content = jsonNode.get("content").asText();
            }
            if (jsonNode.has("username") && !jsonNode.get("username").isNull() && !jsonNode.get("username").asText().isBlank()) {
                username = jsonNode.get("username").asText();
            }
            if (jsonNode.has("playbookId") && !jsonNode.get("playbookId").isNull() && !jsonNode.get("playbookId").asText().isBlank()) {
                playbookId = jsonNode.get("playbookId").asText();
            }
        } catch (Exception e) {
            log.warn("Payload is not valid JSON, treating as plain text.");
        }
        
        // 如果沒有提供 Session ID，代表這是全新的對話，系統會自動建立一個新的 AgentSession。
        if (sessionId == null) {
            log.info("No AgentSession ID provided. Creating a new session for user: {}", username);
            AgentSessionGottenResult newSession = createAgentSessionUseCase.execute(
                new CreateAgentSessionCommand(username, content)
            );
            sessionId = newSession.getSessionId();
            log.info("Created new AgentSession with ID: {}", sessionId);
        }
        
        // 如果請求中帶有 playbookId，則將該劇本綁定至當前的 Session 中，並清除舊的對話歷史。
        if (playbookId != null) {
            log.info("Binding Playbook {} to Session {}", playbookId, sessionId);
            bindPlaybookToSessionUseCase.execute(new com.apiassistant.agent.application.command.BindPlaybookToSessionCommand(sessionId, playbookId));
            chatService.clearHistory(sessionId);
        }
        
        // Call the AI using stream (呼叫 Application Service 取得 Flux 串流)
        final String finalSessionId = sessionId;
        java.util.concurrent.atomic.AtomicReference<String> agentModeRef = new java.util.concurrent.atomic.AtomicReference<>(null);
        java.util.concurrent.atomic.AtomicBoolean hasSentChunks = new java.util.concurrent.atomic.AtomicBoolean(false);
        
        // 訂閱大語言模型的串流輸出
        chatService.chatStream(sessionId, content)
            .subscribe(
                chunk -> {
                    // onNext: 接收到每一小段生成的文字 (chunk)
                    try {
                        if (chunk != null && !chunk.isEmpty()) {
                            hasSentChunks.set(true);
                        }
                        // 首次接收時取得當前對話模式
                        if (agentModeRef.get() == null) {
                            agentModeRef.set(chatService.getSessionAgentMode(finalSessionId));
                        }
                        String agentMode = agentModeRef.get();
                        
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("sessionId", finalSessionId);
                        response.put("role", "ai");
                        response.put("content", chunk);
                        response.put("agentMode", agentMode);
                        response.put("isDelta", true); // 標記這是一小段增量文字
                        
                        // WebSocket session 送出訊息需要 thread-safe，因此加上 synchronized
                        synchronized(session) {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error sending chunk", ex);
                    }
                },
                error -> {
                    // onError: 串流發生錯誤時的處理
                    try {
                        if (agentModeRef.get() == null) {
                            agentModeRef.set(chatService.getSessionAgentMode(finalSessionId));
                        }
                        String agentMode = agentModeRef.get();
                        
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("sessionId", finalSessionId);
                        response.put("role", "ai");
                        response.put("content", "\n[Error: " + error.getMessage() + "]");
                        response.put("agentMode", agentMode);
                        response.put("isFinal", true); // 標記訊息結束
                        
                        synchronized(session) {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error sending error", ex);
                    }
                },
                () -> {
                    // onComplete: 串流正常結束時的處理
                    try {
                        if (agentModeRef.get() == null) {
                            agentModeRef.set(chatService.getSessionAgentMode(finalSessionId));
                        }
                        String agentMode = agentModeRef.get();
                        
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("sessionId", finalSessionId);
                        response.put("role", "ai");
                        
                        // 若這趟呼叫沒有發送任何文字，代表可能被工具呼叫或其他原因攔截了，提示使用者重試
                        if (!hasSentChunks.get()) {
                            log.warn("Stream completed without sending any chunks for session: {}", finalSessionId);
                            response.put("content", "很抱歉，AI 未回傳任何內容，這可能是因為模型仍在處理工具呼叫或發生了預期外的中斷。請再次輸入「OK」重試。");
                        } else {
                            response.put("content", ""); // 空字串表示正常的最終結束訊號
                        }
                        
                        response.put("agentMode", agentMode);
                        response.put("isFinal", true); // 標記訊息結束
                        
                        synchronized(session) {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error sending complete", ex);
                    }
                }
            );
    }

    /**
     * Invoked after the WebSocket connection has been closed by either side.
     * 當 WebSocket 連線關閉時觸發，進行日誌記錄。
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", session.getId());
    }
}
