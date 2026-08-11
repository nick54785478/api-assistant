package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;

import java.util.Optional;

/**
 * Inbound Port (UseCase) for retrieving a Playbook by its ID.
 * 負責依據 ID 取得特定劇本 (Playbook) 的內容。
 */
public interface GetPlaybookUseCase {
    Optional<PlaybookGottenResult> execute(String id);
}
