package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.command.SavePlaybookExecutionLogCommand;
import com.apiassistant.agent.application.port.in.SavePlaybookExecutionLogUseCase;
import com.apiassistant.agent.application.port.out.SavePlaybookExecutionLogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class PlaybookExecutionLogCommandService implements SavePlaybookExecutionLogUseCase {

    private final SavePlaybookExecutionLogPort savePort;

    @Override
    public void execute(SavePlaybookExecutionLogCommand command) {
        savePort.save(command);
    }
}
