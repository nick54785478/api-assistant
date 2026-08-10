package com.apiassistant.agent.application.command;

import lombok.Value;

/**
 * Command object to bind a playbook to an existing AgentSession.
 */
@Value
public class BindPlaybookToSessionCommand {
    String sessionId;
    String playbookId;
}
