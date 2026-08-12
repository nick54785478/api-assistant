package com.apiassistant.agent.application.port.out;

import com.apiassistant.agent.application.dto.PlaybookExecutionLogSearchedResult;

import java.util.List;

/**
 * Outbound port for playbook execution log repository.
 */
public interface PlaybookExecutionLogRepositoryPort {
    List<PlaybookExecutionLogSearchedResult> findBySessionId(String sessionId);
    org.springframework.data.domain.Page<String> findDistinctRunIdsBySessionId(String sessionId, int page, int size);
    List<PlaybookExecutionLogSearchedResult> findByRunIdInOrderByCreatedAtAsc(List<String> runIds);
}
