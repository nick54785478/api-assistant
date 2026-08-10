package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;

import java.util.List;

public interface ListPlaybooksUseCase {
    List<PlaybookGottenResult> listPlaybooks();
}
