package com.apiassistant.agent.application.command;

import lombok.Value;

/**
 * Command object to create a new AgentSession.
 * Naming Rule: V + N + Command (Create + AgentSession + Command)
 */
@Value
public class CreateAgentSessionCommand {
    String username;
    String initialMessage;
}
