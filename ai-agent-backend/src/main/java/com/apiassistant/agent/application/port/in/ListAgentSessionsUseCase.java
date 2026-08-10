package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import java.util.List;

/**
 * UseCase for listing Agent Sessions for a user.
 */
public interface ListAgentSessionsUseCase {
    List<AgentSessionGottenResult> execute(String username);
}
