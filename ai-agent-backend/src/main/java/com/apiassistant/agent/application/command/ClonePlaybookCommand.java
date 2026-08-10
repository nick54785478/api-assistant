package com.apiassistant.agent.application.command;

import lombok.Value;

@Value
public class ClonePlaybookCommand {
    String playbookId;
    String targetAgentSessionId;
}
