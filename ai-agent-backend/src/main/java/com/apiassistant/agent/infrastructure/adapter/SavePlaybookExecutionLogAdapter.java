package com.apiassistant.agent.infrastructure.adapter;

import com.apiassistant.agent.application.command.SavePlaybookExecutionLogCommand;
import com.apiassistant.agent.application.port.out.SavePlaybookExecutionLogPort;
import com.apiassistant.agent.infrastructure.persistence.playbooklog.entity.PlaybookExecutionLogJpaEntity;
import com.apiassistant.agent.infrastructure.persistence.playbooklog.repository.PlaybookExecutionLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter for saving playbook execution logs.
 */
@Component
@RequiredArgsConstructor
class SavePlaybookExecutionLogAdapter implements SavePlaybookExecutionLogPort {

    private final PlaybookExecutionLogJpaRepository jpaRepository;

    @Override
    public void save(SavePlaybookExecutionLogCommand command) {
        PlaybookExecutionLogJpaEntity entity = new PlaybookExecutionLogJpaEntity(
                command.sessionId(),
                command.playbookId(),
                command.runId(),
                command.stepIndex(),
                command.status(),
                command.errorMessage(),
                command.detailMessage(),
                command.occurredAt()
        );
        jpaRepository.save(entity);
    }
}
