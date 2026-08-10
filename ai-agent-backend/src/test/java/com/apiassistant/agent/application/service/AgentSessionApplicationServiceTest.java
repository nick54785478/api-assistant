package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.command.CreateAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import com.apiassistant.agent.application.port.out.AgentSessionRepositoryPort;
import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.agentsession.aggregate.vo.AgentStatus;
import com.apiassistant.agent.domain.agentsession.event.AgentSessionCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentSessionApplicationServiceTest {

    @Mock
    private AgentSessionRepositoryPort repositoryPort;

    private AgentSessionApplicationService applicationService;

    @Captor
    private ArgumentCaptor<AgentSession> sessionCaptor;

    @BeforeEach
    void setUp() {
        applicationService = new AgentSessionApplicationService(repositoryPort);
    }

    @Test
    void execute_shouldCreateSessionAndSaveToRepository() {
        // Arrange
        String initialMessage = "Hello AI!";
        String username = "testuser";
        CreateAgentSessionCommand command = new CreateAgentSessionCommand(username, initialMessage);

        // Act
        AgentSessionGottenResult result = applicationService.execute(command);

        // Assert: Port was called
        verify(repositoryPort, times(1)).save(sessionCaptor.capture());
        AgentSession savedSession = sessionCaptor.getValue();

        // Assert: Domain Aggregate state
        assertNotNull(savedSession.getId());
        assertEquals(AgentStatus.ACTIVE, savedSession.getStatus());
        assertNotNull(savedSession.getCreatedAt());

        // Assert: Domain Event generated
        List<Object> domainEvents = savedSession.pullDomainEvents();
        assertEquals(1, domainEvents.size());
        assertTrue(domainEvents.get(0) instanceof AgentSessionCreatedEvent);
        AgentSessionCreatedEvent event = (AgentSessionCreatedEvent) domainEvents.get(0);
        assertEquals(initialMessage, event.getInitialMessage());
        assertEquals(savedSession.getId(), event.getSessionId());

        // Assert: Result Data Carrier
        assertNotNull(result);
        assertEquals(savedSession.getId().getValue(), result.getSessionId());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(savedSession.getCreatedAt(), result.getCreatedAt());
    }
}
