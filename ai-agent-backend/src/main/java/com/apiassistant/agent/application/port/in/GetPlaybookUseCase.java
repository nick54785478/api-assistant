package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;

import java.util.Optional;

public interface GetPlaybookUseCase {
    Optional<PlaybookGottenResult> getPlaybook(String id);
}
