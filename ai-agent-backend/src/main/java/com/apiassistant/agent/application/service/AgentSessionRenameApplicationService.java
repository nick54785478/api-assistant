package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.assembler.AgentSessionDtoAssembler;
import com.apiassistant.agent.application.command.RenameAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import com.apiassistant.agent.application.port.in.RenameAgentSessionUseCase;
import com.apiassistant.agent.application.port.out.AgentSessionRepositoryPort;
import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.agentsession.aggregate.vo.SessionId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AgentSessionRenameApplicationService implements RenameAgentSessionUseCase {

    private final AgentSessionRepositoryPort repositoryPort;

    @Override
    public AgentSessionGottenResult execute(RenameAgentSessionCommand command) {
        AgentSession session = repositoryPort.findById(SessionId.of(command.getSessionId()))
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + command.getSessionId()));
        
        session.rename(command.getNewName());
        
        repositoryPort.save(session);
        
        return AgentSessionDtoAssembler.toResult(session);
    }
}
