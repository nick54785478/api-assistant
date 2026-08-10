package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.command.CreateAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;

/**
 * Inbound Port / UseCase for creating an AgentSession.
 * Naming Rule: ... + UseCase (CreateAgentSessionUseCase)
 */
public interface CreateAgentSessionUseCase {
    
    /**
     * Executes the creation of a new AgentSession based on the given command.
     * 
     * @param command The command containing initialization data
     * @return AgentSessionGottenResult Pure data carrier for the created session
     */
    AgentSessionGottenResult execute(CreateAgentSessionCommand command);
}
