package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;

import java.util.List;

import com.apiassistant.agent.application.command.CreatePlaybookCommand;

/**
 * Inbound Port (UseCase) for creating a new Playbook.
 * 負責建立全新的劇本 (Playbook)。
 */
public interface CreatePlaybookUseCase {
    
    PlaybookGottenResult execute(CreatePlaybookCommand command);
}
