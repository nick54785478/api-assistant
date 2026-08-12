package com.apiassistant.agent.infrastructure.persistence.playbooklog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "playbook_execution_logs")
@Getter
@Setter
public class PlaybookExecutionLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "playbook_id", nullable = false)
    private String playbookId;

    @Column(name = "run_id", nullable = true)
    private String runId;

    @Column(name = "step_index", nullable = false)
    private Integer stepIndex;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "detail_message", columnDefinition = "TEXT")
    private String detailMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PlaybookExecutionLogJpaEntity() {}

    public PlaybookExecutionLogJpaEntity(String sessionId, String playbookId, String runId, Integer stepIndex, String status, String errorMessage, String detailMessage, Instant createdAt) {
        this.sessionId = sessionId;
        this.playbookId = playbookId;
        this.runId = runId;
        this.stepIndex = stepIndex;
        this.status = status;
        this.errorMessage = errorMessage;
        this.detailMessage = detailMessage;
        this.createdAt = createdAt;
    }
}
