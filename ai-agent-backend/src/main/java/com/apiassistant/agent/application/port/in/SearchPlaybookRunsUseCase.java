package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookRunSearchedResult;
import org.springframework.data.domain.Page;

public interface SearchPlaybookRunsUseCase {
    Page<PlaybookRunSearchedResult> execute(String sessionId, int page, int size);
}
