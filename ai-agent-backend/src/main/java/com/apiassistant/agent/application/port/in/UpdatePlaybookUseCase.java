package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;
import java.util.List;

import com.apiassistant.agent.application.command.UpdatePlaybookCommand;

/**
 * Inbound Port (UseCase) for updating an existing Playbook.
 * 負責更新現有的劇本 (Playbook) 內容。
 */
public interface UpdatePlaybookUseCase {

    PlaybookGottenResult execute(UpdatePlaybookCommand command);
}
