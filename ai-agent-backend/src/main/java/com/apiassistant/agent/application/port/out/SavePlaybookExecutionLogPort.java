package com.apiassistant.agent.application.port.out;

import com.apiassistant.agent.application.command.SavePlaybookExecutionLogCommand;

public interface SavePlaybookExecutionLogPort {
    void save(SavePlaybookExecutionLogCommand command);
}
