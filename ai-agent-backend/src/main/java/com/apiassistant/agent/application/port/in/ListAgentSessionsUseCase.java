package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import java.util.List;

/**
 * Inbound Port (UseCase) for listing Agent Sessions for a user.
 * 負責列出特定使用者的所有 Agent Session。
 */
public interface ListAgentSessionsUseCase {
    List<AgentSessionGottenResult> execute(String username);
}
