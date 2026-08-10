package com.apiassistant.agent.presentation.resource.out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "對話訊息資源")
public class ChatMessageResource {
    @Schema(description = "訊息發送者角色", example = "user 或 ai", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;
    
    @Schema(description = "訊息內容", example = "你好，我是 API Assistant", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
