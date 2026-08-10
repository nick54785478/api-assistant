package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;
import com.apiassistant.agent.application.command.ClonePlaybookCommand;

public interface ClonePlaybookUseCase {
    
    PlaybookGottenResult clonePlaybook(ClonePlaybookCommand command);
}
