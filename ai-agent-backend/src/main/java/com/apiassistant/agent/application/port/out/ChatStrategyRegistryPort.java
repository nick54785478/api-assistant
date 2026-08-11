package com.apiassistant.agent.application.port.out;

import com.apiassistant.agent.application.strategy.ChatStrategy;

import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;

/**
 * Outbound Port for retrieving and managing AI chat strategies.
 * It dynamically selects the appropriate chat strategy (e.g., Playbook mode or General mode) based on the session's state.
 * 負責提供與管理 AI 對話策略 (ChatStrategy) 的 Outbound Port。
 * 根據當前 Session 的狀態動態選擇適當的對話策略 (例如：Playbook 模式或通用模式)。
 */
public interface ChatStrategyRegistryPort {
    
    /**
     * Retrieves the strategy applied to the current chat session.
     *
     * @param session The current Agent Session
     * @param playbook The bound playbook (can be null)
     * @return The ChatStrategy implementation supporting this scenario
     */
    ChatStrategy getStrategy(AgentSession session, Playbook playbook);
}
