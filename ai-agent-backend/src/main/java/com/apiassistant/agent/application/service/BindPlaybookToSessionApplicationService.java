package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.assembler.AgentSessionDtoAssembler;
import com.apiassistant.agent.application.command.BindPlaybookToSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import com.apiassistant.agent.application.port.in.BindPlaybookToSessionUseCase;
import com.apiassistant.agent.application.port.out.AgentSessionRepositoryPort;
import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.agentsession.aggregate.vo.SessionId;
import com.apiassistant.agent.domain.agentsession.exception.AgentSessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class BindPlaybookToSessionApplicationService implements BindPlaybookToSessionUseCase {

    private final AgentSessionRepositoryPort repositoryPort;
    private final ChatApplicationService chatApplicationService;

    @Override
    public AgentSessionGottenResult execute(BindPlaybookToSessionCommand command) {
        AgentSession session = repositoryPort.findById(SessionId.of(command.getSessionId()))
                .orElseThrow(() -> new AgentSessionNotFoundException(SessionId.of(command.getSessionId())));

        session.bindPlaybook(command.getPlaybookId());

        repositoryPort.save(session);
        
        // Clear chat memory to prevent previous playbook's execution history from confusing the AI
        chatApplicationService.clearHistory(command.getSessionId());

        return AgentSessionDtoAssembler.toResult(session);
    }
}
