package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.command.BindPlaybookToSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;

public interface BindPlaybookToSessionUseCase {
    AgentSessionGottenResult execute(BindPlaybookToSessionCommand command);
}
