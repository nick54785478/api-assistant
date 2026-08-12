package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.command.SavePlaybookExecutionLogCommand;

public interface SavePlaybookExecutionLogUseCase {
    void execute(SavePlaybookExecutionLogCommand command);
}
