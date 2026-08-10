package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;
import java.util.List;

import com.apiassistant.agent.application.command.UpdatePlaybookCommand;

/**
 * UseCase for updating an existing Playbook.
 */
public interface UpdatePlaybookUseCase {

    PlaybookGottenResult updatePlaybook(UpdatePlaybookCommand command);
}
