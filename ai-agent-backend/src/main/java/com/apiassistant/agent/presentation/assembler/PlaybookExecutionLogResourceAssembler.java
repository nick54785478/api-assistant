package com.apiassistant.agent.presentation.assembler;

import com.apiassistant.agent.application.dto.PlaybookExecutionLogSearchedResult;
import com.apiassistant.agent.presentation.resource.out.PlaybookExecutionLogSearchedResource;

public class PlaybookExecutionLogResourceAssembler {
    
    private PlaybookExecutionLogResourceAssembler() {}
    
    public static PlaybookExecutionLogSearchedResource toResource(PlaybookExecutionLogSearchedResult result) {
        if (result == null) {
            return null;
        }
        return new PlaybookExecutionLogSearchedResource(
                result.getId(),
                result.getSessionId(),
                result.getPlaybookId(),
                result.getStepIndex(),
                result.getStatus(),
                result.getErrorMessage(),
                result.getCreatedAt()
        );
    }
}
