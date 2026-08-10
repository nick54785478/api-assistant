package com.apiassistant.agent.infrastructure.adapter;

import com.apiassistant.agent.application.strategy.ChatStrategy;
import com.apiassistant.agent.application.port.out.ChatStrategyRegistryPort;
import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ChatStrategyRegistry 的基礎設施層實作。
 * 透過 Spring DI 自動收集所有 ChatStrategy 實作，並提供選擇邏輯。
 */
@Component
public class ChatStrategyRegistryAdapter implements ChatStrategyRegistryPort {

    private final List<ChatStrategy> strategies;

    public ChatStrategyRegistryAdapter(List<ChatStrategy> strategies) {
        this.strategies = strategies != null ? strategies : new java.util.ArrayList<>();
    }

    @Override
    public ChatStrategy getStrategy(AgentSession session, Playbook playbook) {
        for (ChatStrategy strategy : strategies) {
            if (strategy.supports(session, playbook)) {
                return strategy;
            }
        }
        return null;
    }
}
