package com.apiassistant.agent.application.command;

import lombok.Value;

/**
 * Command object to rename an AgentSession.
 */
@Value
public class RenameAgentSessionCommand {
    String sessionId;
    String newName;
}
