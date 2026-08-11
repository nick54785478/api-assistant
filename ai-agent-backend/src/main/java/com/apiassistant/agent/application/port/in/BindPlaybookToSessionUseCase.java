package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.command.BindPlaybookToSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;

/**
 * Inbound Port (UseCase) for binding a Playbook to an Agent Session.
 * 負責將劇本 (Playbook) 綁定到指定的 Agent Session。
 */
public interface BindPlaybookToSessionUseCase {
    AgentSessionGottenResult execute(BindPlaybookToSessionCommand command);
}
