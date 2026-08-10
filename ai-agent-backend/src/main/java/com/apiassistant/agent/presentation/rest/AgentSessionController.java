package com.apiassistant.agent.presentation.rest;

import com.apiassistant.agent.application.command.CreateAgentSessionCommand;
import com.apiassistant.agent.application.command.RenameAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import com.apiassistant.agent.application.port.in.CreateAgentSessionUseCase;
import com.apiassistant.agent.application.port.in.ListAgentSessionsUseCase;
import com.apiassistant.agent.application.port.in.RenameAgentSessionUseCase;
import com.apiassistant.agent.presentation.assembler.AgentSessionResourceAssembler;
import com.apiassistant.agent.presentation.resource.in.CreateAgentSessionResource;
import com.apiassistant.agent.presentation.resource.in.RenameAgentSessionResource;
import com.apiassistant.agent.presentation.resource.out.AgentSessionCreatedResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for AgentSession operations.
 * Relies exclusively on Inbound Ports (UseCases) and Assemblers.
 */
@RestController
@RequestMapping("/api/v1/agent-sessions")
@RequiredArgsConstructor
@Tag(name = "Agent Session API", description = "AI 助手會話管理相關介面")
public class AgentSessionController {

    private final CreateAgentSessionUseCase createAgentSessionUseCase;
    private final ListAgentSessionsUseCase listAgentSessionsUseCase;
    private final RenameAgentSessionUseCase renameAgentSessionUseCase;
    private final com.apiassistant.agent.application.service.ChatApplicationService chatApplicationService;

    @Operation(summary = "建立新會話", description = "建立一個全新的 AI 助手會話，並可選填初始訊息。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "會話建立成功")
    })
    @PostMapping
    public ResponseEntity<AgentSessionCreatedResource> createSession(@RequestBody CreateAgentSessionResource request) {
        CreateAgentSessionCommand command = AgentSessionResourceAssembler.toCommand(request);
        AgentSessionGottenResult result = createAgentSessionUseCase.execute(command);
        AgentSessionCreatedResource response = AgentSessionResourceAssembler.toResource(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "獲取會話列表", description = "根據使用者名稱查詢其擁有的所有會話。")
    @GetMapping
    public ResponseEntity<List<AgentSessionCreatedResource>> listSessions(
            @Parameter(description = "使用者名稱", required = true) @RequestParam String username) {
        List<AgentSessionGottenResult> results = listAgentSessionsUseCase.execute(username);
        List<AgentSessionCreatedResource> response = results.stream()
                .map(AgentSessionResourceAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "重新命名會話", description = "變更指定會話的名稱。")
    @PatchMapping("/{id}/name")
    public ResponseEntity<AgentSessionCreatedResource> renameSession(
            @Parameter(description = "會話 ID", required = true) @PathVariable String id, 
            @RequestBody RenameAgentSessionResource request) {
        RenameAgentSessionCommand command = new RenameAgentSessionCommand(id, request.getName());
        AgentSessionGottenResult result = renameAgentSessionUseCase.execute(command);
        AgentSessionCreatedResource response = AgentSessionResourceAssembler.toResource(result);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "獲取對話歷史紀錄", description = "獲取指定會話的所有歷史對話訊息。")
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<com.apiassistant.agent.presentation.resource.out.ChatMessageResource>> getMessages(
            @Parameter(description = "會話 ID", required = true) @PathVariable String id) {
        // Using ChatApplicationService directly to fetch in-memory messages for simplicity
        List<org.springframework.ai.chat.messages.Message> history = chatApplicationService.getHistory(id);
        List<com.apiassistant.agent.presentation.resource.out.ChatMessageResource> response = history.stream()
                .map(msg -> new com.apiassistant.agent.presentation.resource.out.ChatMessageResource(
                        msg.getMessageType().name().toLowerCase(),
                        msg.getText()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
