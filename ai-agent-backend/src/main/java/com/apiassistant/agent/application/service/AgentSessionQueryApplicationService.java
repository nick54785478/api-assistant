package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.assembler.AgentSessionDtoAssembler;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import com.apiassistant.agent.application.port.in.ListAgentSessionsUseCase;
import com.apiassistant.agent.application.port.out.AgentSessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentSessionQueryApplicationService implements ListAgentSessionsUseCase {

    private final AgentSessionRepositoryPort repositoryPort;

    @Override
    public List<AgentSessionGottenResult> execute(String username) {
        return repositoryPort.findByUsername(username).stream()
                .map(AgentSessionDtoAssembler::toResult)
                .collect(Collectors.toList());
    }
}
