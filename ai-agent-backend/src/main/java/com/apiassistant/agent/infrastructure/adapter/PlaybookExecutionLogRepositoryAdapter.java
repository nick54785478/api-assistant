package com.apiassistant.agent.infrastructure.adapter;

import com.apiassistant.agent.application.dto.PlaybookExecutionLogSearchedResult;
import com.apiassistant.agent.application.port.out.PlaybookExecutionLogRepositoryPort;
import com.apiassistant.agent.infrastructure.persistence.playbooklog.repository.PlaybookExecutionLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter for playbook execution log repository.
 */
@Component
@RequiredArgsConstructor
class PlaybookExecutionLogRepositoryAdapter implements PlaybookExecutionLogRepositoryPort {

    private final PlaybookExecutionLogJpaRepository jpaRepository;

    @Override
    public List<PlaybookExecutionLogSearchedResult> findBySessionId(String sessionId) {
        return jpaRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(entity -> new PlaybookExecutionLogSearchedResult(
                        entity.getId(),
                        entity.getSessionId(),
                        entity.getPlaybookId(),
                        entity.getRunId(),
                        entity.getStepIndex(),
                        entity.getStatus(),
                        entity.getErrorMessage(),
                        entity.getDetailMessage(),
                        entity.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    public org.springframework.data.domain.Page<String> findDistinctRunIdsBySessionId(String sessionId, int page, int size) {
        return jpaRepository.findDistinctRunIdsBySessionId(sessionId, org.springframework.data.domain.PageRequest.of(page, size));
    }
    
    @Override
    public List<PlaybookExecutionLogSearchedResult> findByRunIdInOrderByCreatedAtAsc(List<String> runIds) {
        if (runIds == null || runIds.isEmpty()) return List.of();
        
        return jpaRepository.findByRunIdInOrderByCreatedAtAsc(runIds).stream()
                .map(entity -> new PlaybookExecutionLogSearchedResult(
                        entity.getId(),
                        entity.getSessionId(),
                        entity.getPlaybookId(),
                        entity.getRunId(),
                        entity.getStepIndex(),
                        entity.getStatus(),
                        entity.getErrorMessage(),
                        entity.getDetailMessage(),
                        entity.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
