package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;
import com.apiassistant.agent.application.command.ClonePlaybookCommand;

/**
 * Inbound Port (UseCase) for cloning an existing Playbook.
 * 負責複製現有的劇本 (Playbook)。
 */
public interface ClonePlaybookUseCase {
    
    PlaybookGottenResult execute(ClonePlaybookCommand command);
}
