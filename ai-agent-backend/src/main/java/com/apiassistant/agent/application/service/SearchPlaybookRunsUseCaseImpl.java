package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.dto.PlaybookExecutionLogSearchedResult;
import com.apiassistant.agent.application.dto.PlaybookRunSearchedResult;
import com.apiassistant.agent.application.port.in.SearchPlaybookRunsUseCase;
import com.apiassistant.agent.application.port.out.PlaybookExecutionLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchPlaybookRunsUseCaseImpl implements SearchPlaybookRunsUseCase {

    private final PlaybookExecutionLogRepositoryPort repositoryPort;

    @Override
    public Page<PlaybookRunSearchedResult> execute(String sessionId, int page, int size) {
        Page<String> runIdsPage = repositoryPort.findDistinctRunIdsBySessionId(sessionId, page, size);
        
        if (runIdsPage.isEmpty()) {
            return Page.empty(runIdsPage.getPageable());
        }

        List<String> runIds = runIdsPage.getContent();
        List<PlaybookExecutionLogSearchedResult> logs = repositoryPort.findByRunIdInOrderByCreatedAtAsc(runIds);
        
        Map<String, List<PlaybookExecutionLogSearchedResult>> logsByRun = logs.stream()
                .filter(log -> log.getRunId() != null)
                .collect(Collectors.groupingBy(PlaybookExecutionLogSearchedResult::getRunId));
                
        List<PlaybookRunSearchedResult> results = new ArrayList<>();
        
        for (String runId : runIds) {
            List<PlaybookExecutionLogSearchedResult> runLogs = logsByRun.getOrDefault(runId, List.of());
            if (runLogs.isEmpty()) continue;
            
            String playbookId = runLogs.get(0).getPlaybookId();
            int totalSteps = runLogs.size();
            
            // Determine status based on the latest log of the run
            PlaybookExecutionLogSearchedResult lastLog = runLogs.get(runLogs.size() - 1);
            String status = "COMPLETED"; // Default assume completed if no failure
            for (PlaybookExecutionLogSearchedResult log : runLogs) {
                if ("FAILED".equals(log.getStatus())) {
                    status = "FAILED";
                    break;
                }
            }
            // If the latest step is not a failure, but maybe playbook has more steps, it might be IN_PROGRESS
            // For simplicity, we just use SUCCESS/FAILED.
            
            results.add(new PlaybookRunSearchedResult(
                    runId,
                    playbookId,
                    status,
                    totalSteps,
                    runLogs.get(0).getCreatedAt(),
                    runLogs
            ));
        }
        
        return new PageImpl<>(results, runIdsPage.getPageable(), runIdsPage.getTotalElements());
    }
}
