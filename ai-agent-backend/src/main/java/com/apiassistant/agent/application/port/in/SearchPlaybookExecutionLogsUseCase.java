package com.apiassistant.agent.application.port.in;

import com.apiassistant.agent.application.dto.PlaybookExecutionLogSearchedResult;

import java.util.List;

/**
 * Inbound port for searching playbook execution logs.
 */
public interface SearchPlaybookExecutionLogsUseCase {
    List<PlaybookExecutionLogSearchedResult> execute(String sessionId);
}
