package com.apiassistant.agent.presentation.listener;

import com.apiassistant.agent.application.command.SavePlaybookExecutionLogCommand;
import com.apiassistant.agent.application.port.in.SavePlaybookExecutionLogUseCase;
import com.apiassistant.agent.domain.agentsession.event.PlaybookStepExecutedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter listening to domain events and delegating to application use case.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaybookExecutionEventListener {

    private final SavePlaybookExecutionLogUseCase useCase;

    @Async
    @EventListener
    public void handlePlaybookStepExecutedEvent(PlaybookStepExecutedEvent event) {
        log.info("Received PlaybookStepExecutedEvent in Presentation Layer: session={}, step={}, status={}", 
                 event.sessionId(), event.stepIndex(), event.status());
                 
        SavePlaybookExecutionLogCommand command = new SavePlaybookExecutionLogCommand(
                event.sessionId(),
                event.playbookId(),
                event.runId(),
                event.stepIndex(),
                event.status(),
                event.errorMessage(),
                event.aiMessage(),
                event.occurredAt()
        );
        
        useCase.execute(command);
    }
}
