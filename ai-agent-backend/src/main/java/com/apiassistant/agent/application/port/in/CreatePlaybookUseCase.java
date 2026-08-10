package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;

import java.util.List;

import com.apiassistant.agent.application.command.CreatePlaybookCommand;

public interface CreatePlaybookUseCase {
    
    PlaybookGottenResult createPlaybook(CreatePlaybookCommand command);
}
