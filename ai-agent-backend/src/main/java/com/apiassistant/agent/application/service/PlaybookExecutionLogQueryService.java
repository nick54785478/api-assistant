package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.dto.PlaybookExecutionLogSearchedResult;
import com.apiassistant.agent.application.port.in.SearchPlaybookExecutionLogsUseCase;
import com.apiassistant.agent.application.port.out.PlaybookExecutionLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementing the use case for searching playbook execution logs.
 */
@Service
@RequiredArgsConstructor
class PlaybookExecutionLogQueryService implements SearchPlaybookExecutionLogsUseCase {

    private final PlaybookExecutionLogRepositoryPort repositoryPort;

    @Override
    public List<PlaybookExecutionLogSearchedResult> execute(String sessionId) {
        return repositoryPort.findBySessionId(sessionId);
    }
}
