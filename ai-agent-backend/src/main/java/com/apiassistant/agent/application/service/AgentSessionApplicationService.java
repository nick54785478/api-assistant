package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.assembler.AgentSessionDtoAssembler;
import com.apiassistant.agent.application.command.CreateAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import com.apiassistant.agent.application.port.in.CreateAgentSessionUseCase;
import com.apiassistant.agent.application.port.out.AgentSessionRepositoryPort;
import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application Service implementing the UseCase.
 * Naming Rule: ... + ApplicationService
 * Visibility: package-private to prevent outer layers from bypassing the Port interface.
 */
@Service
@RequiredArgsConstructor
class AgentSessionApplicationService implements CreateAgentSessionUseCase {

    private final AgentSessionRepositoryPort repositoryPort;

    @Override
    public AgentSessionGottenResult execute(CreateAgentSessionCommand command) {
        // 1. Create Domain Aggregate (this also generates the Domain Event internally)
        AgentSession session = AgentSession.create(command.getUsername(), command.getInitialMessage());
        
        // 2. Persist using the Outbound Port
        repositoryPort.save(session);
        
        // 3. Assemble and return Pure Data Carrier (DTO)
        return AgentSessionDtoAssembler.toResult(session);
    }
}
