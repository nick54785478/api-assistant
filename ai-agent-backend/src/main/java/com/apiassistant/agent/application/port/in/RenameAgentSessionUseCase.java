package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.command.RenameAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;

/**
 * Inbound Port (UseCase) for renaming an Agent Session.
 * 負責重新命名 Agent Session。
 */
public interface RenameAgentSessionUseCase {
    AgentSessionGottenResult execute(RenameAgentSessionCommand command);
}
