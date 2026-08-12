package com.apiassistant.agent.infrastructure.persistence.playbooklog.repository;

import com.apiassistant.agent.infrastructure.persistence.playbooklog.entity.PlaybookExecutionLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaybookExecutionLogJpaRepository extends JpaRepository<PlaybookExecutionLogJpaEntity, UUID> {
    
    /**
     * Find all execution logs for a specific session, ordered by creation time ascending.
     */
    List<PlaybookExecutionLogJpaEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    /**
     * Find paginated runIds for a session ordered by latest log creation time descending.
     */
    @org.springframework.data.jpa.repository.Query("SELECT p.runId FROM PlaybookExecutionLogJpaEntity p WHERE p.sessionId = :sessionId GROUP BY p.runId ORDER BY MAX(p.createdAt) DESC")
    org.springframework.data.domain.Page<String> findDistinctRunIdsBySessionId(@org.springframework.data.repository.query.Param("sessionId") String sessionId, org.springframework.data.domain.Pageable pageable);

    /**
     * Find logs for specific runIds.
     */
    List<PlaybookExecutionLogJpaEntity> findByRunIdInOrderByCreatedAtAsc(List<String> runIds);
}
