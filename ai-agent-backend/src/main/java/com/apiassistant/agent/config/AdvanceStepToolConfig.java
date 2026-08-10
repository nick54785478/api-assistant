package com.apiassistant.agent.config;

import com.apiassistant.agent.application.port.out.AgentSessionRepositoryPort;
import com.apiassistant.agent.domain.agentsession.aggregate.vo.SessionId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Slf4j
@Configuration
public class AdvanceStepToolConfig {

    public record AdvanceStepRequest(String sessionId) {}

    @Bean
    @Description("當且僅當『使用者明確同意』進入下一步時，才呼叫此工具。此工具會將劇本狀態推進至下一步驟。必須傳入目前的 sessionId。")
    public Function<AdvanceStepRequest, String> advancePlaybookStep(AgentSessionRepositoryPort agentSessionRepositoryPort) {
        return request -> {
            log.info("Tool 'advance_playbook_step' called for session: {}", request.sessionId());
            try {
                agentSessionRepositoryPort.findById(SessionId.of(request.sessionId())).ifPresent(session -> {
                    session.advanceStep();
                    agentSessionRepositoryPort.save(session);
                    log.info("Session {} advanced to step index {}", request.sessionId(), session.getCurrentStepIndex());
                });
                return "推進成功！請回覆使用者：「已成功推進至下一步驟，請問要現在開始執行嗎？」";
            } catch (Exception e) {
                log.error("Failed to advance step for session " + request.sessionId(), e);
                return "推進失敗：" + e.getMessage();
            }
        };
    }
}
