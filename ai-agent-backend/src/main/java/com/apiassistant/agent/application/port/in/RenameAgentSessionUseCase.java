package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.command.RenameAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;

/**
 * UseCase for renaming an Agent Session.
 */
public interface RenameAgentSessionUseCase {
    AgentSessionGottenResult execute(RenameAgentSessionCommand command);
}
