package com.apiassistant.agent.application.assembler;

import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;

/**
 * Assembler responsible for mapping Domain models to Application DTOs.
 */
public class AgentSessionDtoAssembler {
    
    public static AgentSessionGottenResult toResult(AgentSession session) {
        if (session == null) {
            return null;
        }
        return new AgentSessionGottenResult(
                session.getId().getValue(),
                session.getName(),
                session.getStatus().name(),
                session.getCreatedAt(),
                session.getPlaybookId()
        );
    }
}
