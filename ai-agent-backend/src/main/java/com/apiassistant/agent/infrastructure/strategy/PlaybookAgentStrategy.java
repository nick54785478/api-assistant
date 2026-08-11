package com.apiassistant.agent.infrastructure.strategy;

import com.apiassistant.agent.application.strategy.ChatStrategy;

import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.playbook.aggregate.entity.PlaybookStep;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 處理「劇本執行模式」的 Agent 策略。
 * 當 Session 綁定了劇本，且劇本步驟尚未執行完畢時，此策略將被自動啟動，接管使用者的輸入。
 */
@Slf4j
@Component
public class PlaybookAgentStrategy implements ChatStrategy {

    private static final Set<String> APPROVAL_KEYWORDS = Set.of("ok", "好", "繼續", "下一步", "next", "y", "yes", "proceed", "完成", "done");
    private static final Set<String> CANCEL_KEYWORDS = Set.of("取消", "退出", "停止", "cancel", "abort", "quit", "exit");

    /**
     * 【策略路由條件】
     * 只有當前 Session 綁定了 playbook，且進度尚未完成時，此策略才會啟動 (回傳 true)。
     */
    @Override
    public boolean supports(AgentSession session, Playbook playbook) {
        return playbook != null && session != null && session.getCurrentStepIndex() < playbook.getSteps().size();
    }

    @Override
    public PreProcessResult preProcess(String sessionId, String userMessage, AgentSession session, Playbook playbook) {
        String lowerMsg = userMessage.trim().toLowerCase();
        boolean isApproval = APPROVAL_KEYWORDS.contains(lowerMsg);
        boolean isCancel = CANCEL_KEYWORDS.contains(lowerMsg);
                             
        if (isCancel) {
            session.unbindPlaybook();
            log.info("Unbound playbook from session {} due to user cancellation", sessionId);
            return PreProcessResult.SAVE_AND_CLEAR; // require clearing chat memory
        }
                             
        if (isApproval) {
            session.advanceStep();
            log.info("Advanced session {} to step {}", sessionId, session.getCurrentStepIndex());
            return PreProcessResult.SAVE_AND_CLEAR; // 清除歷史記憶，避免 Ollama 多輪 ToolCall 崩潰，上下文由 service 提取後手動注入
        }
        return PreProcessResult.NONE;
    }

    @Override
    public String getSystemPrompt(String defaultSystemPrompt, AgentSession session, Playbook playbook, List<String> allowedToolNames, String sessionId) {
        int stepIndex = session.getCurrentStepIndex();
        
        // Edge case: preProcess just advanced the step, and it has finished the playbook now
        if (stepIndex >= playbook.getSteps().size()) {
            session.unbindPlaybook(); // 自動解除綁定
            return defaultSystemPrompt + "\n\n【系統指示】：此測試劇本的所有步驟已全部順利執行完畢！請直接回覆使用者：「🎉 所有測試步驟已執行完畢！劇本順利完成。模式已切換回一般助手。請問還有什麼可以幫您的嗎？」";
        }
        
        if (session.getPlaybookId() == null) {
            return defaultSystemPrompt + "\n\n【系統指示】：劇本執行已中斷。請回覆：「🛑 已退出劇本執行模式。已切換回一般助手。請問還有什麼可以幫您的嗎？」";
        }
        
        PlaybookStep step = playbook.getSteps().get(stepIndex);
        
        // --- 精簡化的 System Prompt，降低 token 數量以提高 Tool Calling 命中率 ---
        StringBuilder sb = new StringBuilder();
        sb.append(defaultSystemPrompt).append("\n\n");
        sb.append("=== 劇本執行模式 ===").append("\n");
        sb.append("SessionID: ").append(sessionId).append("\n");
        sb.append("劇本: ").append(playbook.getName()).append("\n\n");
        
        // 精簡步驟一覽（只顯示已完成和當前步驟，節省 token）
        sb.append("進度: ");
        for (int i = 0; i < playbook.getSteps().size(); i++) {
            if (i == stepIndex) {
                sb.append("[👉").append(i + 1).append("]");
            } else if (i < stepIndex) {
                sb.append("[✅").append(i + 1).append("]");
            } else {
                sb.append("[").append(i + 1).append("]");
            }
        }
        sb.append("\n\n");
        
        // 當前步驟指示
        sb.append("=== 當前任務 (第").append(stepIndex + 1).append("步): ").append(step.getName()).append(" ===").append("\n");
        sb.append(step.getDescription()).append("\n");
        
        if (step.getRequiredTool() != null && !step.getRequiredTool().isBlank()) {
            String resolvedToolName = resolveToolName(step.getRequiredTool(), allowedToolNames);
            sb.append("\n必須呼叫的工具: ").append(resolvedToolName).append("\n");
        }
        
        if (step.getCustomInputs() != null && !step.getCustomInputs().isEmpty()) {
            sb.append("\n預設參數 (直接使用，不需詢問使用者):\n");
            for (var param : step.getCustomInputs()) {
                sb.append("  ").append(param.getKey()).append(": ").append(param.getValue()).append("\n");
            }
        }
        
        if (step.getResponseInstructions() != null && !step.getResponseInstructions().isBlank()) {
            sb.append("\n回覆要求: ").append(step.getResponseInstructions()).append("\n");
        }
        
        sb.append("\n=== 規則 ===");
        sb.append("\n1. 使用 Tool Calling 機制呼叫工具，不要輸出 JSON 文字。");
        sb.append("\n2. 劇本預設參數直接使用。來自已完成步驟的結果（如 Token）從對話歷史中提取，禁止編造。");
        sb.append("\n3. 當任務完成並報告結果後，請務必在回覆的最下方附上以下操作提示（請一字不漏地加上）：\n");
        if (stepIndex == playbook.getSteps().size() - 1) {
            sb.append("   💡 **劇本操作提示**：\n   - 輸入 **完成** 或 **退出**：結束劇本模式並清除對話上下文");
        } else {
            sb.append("   💡 **劇本操作提示**：\n   - 輸入 **OK** 或 **下一步**：繼續執行下一個步驟\n   - 輸入 **取消** 或 **退出**：終止當前劇本模式");
        }
        sb.append("\n4. 用繁體中文自然語言回覆，**只能根據工具回傳的真實資料進行報告，絕對禁止自行編造、腦補任何未回傳的欄位（如員工姓名、編號等）**。");
        
        return sb.toString();
    }
    
    /**
     * 解析工具的實際名稱：先嘗試精確匹配，再嘗試後綴匹配（支援 session-scoped 前綴）。
     */
    private String resolveToolName(String expectedName, List<String> allowedToolNames) {
        if (allowedToolNames == null || allowedToolNames.isEmpty()) {
            return expectedName;
        }
        return allowedToolNames.stream()
                .filter(name -> name.equals(expectedName))
                .findFirst()
                .orElseGet(() -> allowedToolNames.stream()
                        .filter(name -> name.endsWith("_" + expectedName))
                        .findFirst()
                        .orElse(expectedName));
    }

    @Override
    public List<ToolCallback> getActiveTools(AgentSession session, Playbook playbook, List<ToolCallback> allTools, List<String> allowedToolNames) {
        int stepIndex = session.getCurrentStepIndex();
        
        if (session.getPlaybookId() == null) {
            return getGeneralActiveTools(allTools, allowedToolNames);
        }
        
        // Edge case: preProcess advanced step and finished playbook
        if (stepIndex >= playbook.getSteps().size()) {
            // Act like general strategy in this edge case
            return getGeneralActiveTools(allTools, allowedToolNames);
        }
        
        PlaybookStep currentStep = playbook.getSteps().get(stepIndex);
        List<ToolCallback> activeCallbacks = new ArrayList<>();
        
        if (currentStep.getRequiredTool() != null && !currentStep.getRequiredTool().isBlank()) {
            String requiredName = currentStep.getRequiredTool();
            // First, find the exact allowed name for this session
            String targetAllowedName = resolveToolName(requiredName, allowedToolNames);
            
            // Then find the callback that matches this exact target name
            allTools.stream()
                    .filter(callback -> callback.getToolDefinition().name().equals(targetAllowedName))
                    .findFirst()
                    .ifPresent(activeCallbacks::add);
        } else {
            // Fallback if no specific tool is required
            activeCallbacks.addAll(getGeneralActiveTools(allTools, allowedToolNames));
        }
        
        return activeCallbacks;
    }
    
    private List<ToolCallback> getGeneralActiveTools(List<ToolCallback> allTools, List<String> allowedToolNames) {
        List<ToolCallback> activeCallbacks = new ArrayList<>();
        if (allowedToolNames != null && !allowedToolNames.isEmpty()) {
            for (ToolCallback callback : allTools) {
                if (allowedToolNames.contains(callback.getToolDefinition().name())) {
                    activeCallbacks.add(callback);
                }
            }
        } else {
            for (ToolCallback callback : allTools) {
                if (!callback.getToolDefinition().name().startsWith("s_")) {
                    activeCallbacks.add(callback);
                }
            }
        }
        return activeCallbacks;
    }
}
