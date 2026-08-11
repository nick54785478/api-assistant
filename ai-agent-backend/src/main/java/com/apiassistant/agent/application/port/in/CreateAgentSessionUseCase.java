package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.command.CreateAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;

/**
 * Inbound Port (UseCase) for creating a new Agent Session.
 * 負責建立新的 Agent Session。
 */
public interface CreateAgentSessionUseCase {
    
    /**
     * Executes the creation of a new Agent Session based on the given command.
     * 
     * @param command The command containing initialization data
     * @return AgentSessionGottenResult Pure data carrier for the created session
     */
    AgentSessionGottenResult execute(CreateAgentSessionCommand command);
}
