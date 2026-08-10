package com.apiassistant.agent.presentation.rest;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;
import com.apiassistant.agent.application.command.CreatePlaybookCommand;
import com.apiassistant.agent.application.command.UpdatePlaybookCommand;
import com.apiassistant.agent.application.command.ClonePlaybookCommand;
import com.apiassistant.agent.application.port.in.CreatePlaybookUseCase;
import com.apiassistant.agent.application.port.in.GetPlaybookUseCase;
import com.apiassistant.agent.application.port.in.ListPlaybooksUseCase;
import com.apiassistant.agent.presentation.assembler.PlaybookResourceAssembler;
import com.apiassistant.agent.presentation.resource.in.CreatePlaybookResource;
import com.apiassistant.agent.presentation.resource.out.PlaybookResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/playbooks")
@RequiredArgsConstructor
@Tag(name = "Playbook", description = "Endpoints for managing Agent Playbooks / Workflows")
public class PlaybookController {

    private final CreatePlaybookUseCase createPlaybookUseCase;
    private final ListPlaybooksUseCase listPlaybooksUseCase;
    private final GetPlaybookUseCase getPlaybookUseCase;
    private final com.apiassistant.agent.application.port.in.UpdatePlaybookUseCase updatePlaybookUseCase;
    private final com.apiassistant.agent.application.port.in.ClonePlaybookUseCase clonePlaybookUseCase;

    @PostMapping
    @Operation(summary = "Create a new playbook")
    public ResponseEntity<PlaybookResource> createPlaybook(@RequestBody CreatePlaybookResource request) {
        CreatePlaybookCommand command = PlaybookResourceAssembler.toCommand(request);
        PlaybookGottenResult result = createPlaybookUseCase.createPlaybook(command);
        return ResponseEntity.ok(PlaybookResourceAssembler.toResource(result));
    }

    @GetMapping
    @Operation(summary = "List all playbooks")
    public ResponseEntity<List<PlaybookResource>> listPlaybooks() {
        List<PlaybookResource> resources = listPlaybooksUseCase.listPlaybooks().stream()
                .map(PlaybookResourceAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get playbook by ID")
    public ResponseEntity<PlaybookResource> getPlaybook(@PathVariable String id) {
        return getPlaybookUseCase.getPlaybook(id)
                .map(PlaybookResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing playbook")
    public ResponseEntity<PlaybookResource> updatePlaybook(@PathVariable String id, @RequestBody com.apiassistant.agent.presentation.resource.in.UpdatePlaybookResource request) {
        UpdatePlaybookCommand command = PlaybookResourceAssembler.toCommand(id, request);
        PlaybookGottenResult result = updatePlaybookUseCase.updatePlaybook(command);
        return ResponseEntity.ok(PlaybookResourceAssembler.toResource(result));
    }

    @PostMapping("/{id}/clone")
    @Operation(summary = "Clone an existing playbook")
    public ResponseEntity<PlaybookResource> clonePlaybook(
            @PathVariable String id,
            @RequestParam(required = false) String targetAgentSessionId) {
        ClonePlaybookCommand command = new ClonePlaybookCommand(id, targetAgentSessionId);
        PlaybookGottenResult result = clonePlaybookUseCase.clonePlaybook(command);
        return ResponseEntity.ok(PlaybookResourceAssembler.toResource(result));
    }
}
