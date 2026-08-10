package com.apiassistant.agent.presentation.resource.in;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "重新命名會話的請求資源")
public class RenameAgentSessionResource {
    @Schema(description = "新名稱", example = "測試會話", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
