package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;

import java.util.List;

/**
 * Inbound Port (UseCase) for listing all Playbooks.
 * 負責列出系統中所有可用的劇本 (Playbook)。
 */
public interface ListPlaybooksUseCase {
    List<PlaybookGottenResult> execute();
}
