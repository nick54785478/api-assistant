package com.apiassistant.agent.presentation.assembler;

import com.apiassistant.agent.application.command.CreateAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import com.apiassistant.agent.presentation.resource.in.CreateAgentSessionResource;
import com.apiassistant.agent.presentation.resource.out.AgentSessionCreatedResource;

/**
 * Assembler responsible for mapping Presentation Resources to Application Commands/DTOs.
 */
public class AgentSessionResourceAssembler {

    public static CreateAgentSessionCommand toCommand(CreateAgentSessionResource resource) {
        return new CreateAgentSessionCommand(resource.getUsername(), resource.getInitialMessage());
    }

    public static AgentSessionCreatedResource toResource(AgentSessionGottenResult result) {
        if (result == null) {
            return null;
        }
        return new AgentSessionCreatedResource(
                result.getSessionId(),
                result.getName(),
                result.getStatus(),
                result.getCreatedAt(),
                result.getPlaybookId()
        );
    }
}
