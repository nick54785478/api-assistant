package com.apiassistant.agent.infrastructure.strategy;

import com.apiassistant.agent.application.strategy.ChatStrategy;

import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 處理「一般 API 助手模式」的 Agent 策略。
 * 作為系統的兜底 (Fallback) 策略，當沒有綁定劇本，或是劇本已經執行完畢時，此策略將被啟動。
 */
@Component
public class GeneralAgentStrategy implements ChatStrategy {

    /**
     * 【策略路由條件】
     * 如果沒有劇本，或是劇本已經跑完了，都適用此一般對話策略。
     */
    @Override
    public boolean supports(AgentSession session, Playbook playbook) {
        // 兜底策略：如果沒有劇本，或是劇本已經跑完了，都適用此策略
        return playbook == null || session == null || session.getCurrentStepIndex() >= playbook.getSteps().size();
    }

    @Override
    public PreProcessResult preProcess(String sessionId, String userMessage, AgentSession session, Playbook playbook) {
        // 一般模式下不需要攔截進度
        return PreProcessResult.NONE;
    }

    @Override
    public String getSystemPrompt(String defaultSystemPrompt, AgentSession session, Playbook playbook, List<String> allowedToolNames, String sessionId) {
        if (playbook != null && session != null && session.getCurrentStepIndex() >= playbook.getSteps().size()) {
            return defaultSystemPrompt + "\n\n【系統指示】：此測試劇本的所有步驟已全部執行完畢！\n若使用者無特別指示，請回覆：「🎉 所有測試步驟已執行完畢！劇本順利完成。」。\n若使用者有新的需求或要求呼叫特定 API，請恢復正常助手模式，根據其需求呼叫對應工具。";
        }
        return defaultSystemPrompt;
    }

    @Override
    public List<ToolCallback> getActiveTools(AgentSession session, Playbook playbook, List<ToolCallback> allTools, List<String> allowedToolNames) {
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
