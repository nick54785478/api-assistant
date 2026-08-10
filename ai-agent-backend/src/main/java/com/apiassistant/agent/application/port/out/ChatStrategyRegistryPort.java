package com.apiassistant.agent.application.port.out;

import com.apiassistant.agent.application.strategy.ChatStrategy;

import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;

/**
 * 處理與 AI 代理對話的策略 Registry 介面。
 * 負責依據 Session 狀態選擇對應的 ChatStrategy。
 */
public interface ChatStrategyRegistryPort {
    
    /**
     * 取得特定狀態下適用的對話策略
     *
     * @param session 當前對話的 Session
     * @param playbook 當前綁定的劇本 (可為 null)
     * @return 支援該狀態的 ChatStrategy 實例
     */
    ChatStrategy getStrategy(AgentSession session, Playbook playbook);
}
