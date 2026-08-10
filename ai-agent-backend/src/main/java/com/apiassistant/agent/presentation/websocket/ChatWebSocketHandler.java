package com.apiassistant.agent.presentation.websocket;

import com.apiassistant.agent.application.command.CreateAgentSessionCommand;
import com.apiassistant.agent.application.dto.AgentSessionGottenResult;
import com.apiassistant.agent.application.port.in.CreateAgentSessionUseCase;
import com.apiassistant.agent.application.service.ChatApplicationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Inbound Adapter for handling WebSocket Chat Messages.
 * Delegates to ChatApplicationService for LLM interactions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatApplicationService chatService;
    private final CreateAgentSessionUseCase createAgentSessionUseCase;
    private final com.apiassistant.agent.application.port.in.BindPlaybookToSessionUseCase bindPlaybookToSessionUseCase;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        ObjectNode response = objectMapper.createObjectNode();
        response.put("role", "ai");
        response.put("content", "連線成功！Ollama 已準備就緒。");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message: {}", payload);
        
        String sessionId = null;
        String content = payload;
        String username = "Guest";
        String playbookId = null;
        
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            if (jsonNode.has("sessionId") && !jsonNode.get("sessionId").isNull() && !jsonNode.get("sessionId").asText().isBlank()) {
                sessionId = jsonNode.get("sessionId").asText();
            }
            if (jsonNode.has("content")) {
                content = jsonNode.get("content").asText();
            }
            if (jsonNode.has("username") && !jsonNode.get("username").isNull() && !jsonNode.get("username").asText().isBlank()) {
                username = jsonNode.get("username").asText();
            }
            if (jsonNode.has("playbookId") && !jsonNode.get("playbookId").isNull() && !jsonNode.get("playbookId").asText().isBlank()) {
                playbookId = jsonNode.get("playbookId").asText();
            }
        } catch (Exception e) {
            log.warn("Payload is not valid JSON, treating as plain text.");
        }
        
        if (sessionId == null) {
            log.info("No AgentSession ID provided. Creating a new session for user: {}", username);
            AgentSessionGottenResult newSession = createAgentSessionUseCase.execute(
                new CreateAgentSessionCommand(username, content)
            );
            sessionId = newSession.getSessionId();
            log.info("Created new AgentSession with ID: {}", sessionId);
        }
        
        if (playbookId != null) {
            log.info("Binding Playbook {} to Session {}", playbookId, sessionId);
            bindPlaybookToSessionUseCase.execute(new com.apiassistant.agent.application.command.BindPlaybookToSessionCommand(sessionId, playbookId));
            chatService.clearHistory(sessionId);
        }
        
        // Call the AI using stream
        final String finalSessionId = sessionId;
        java.util.concurrent.atomic.AtomicReference<String> agentModeRef = new java.util.concurrent.atomic.AtomicReference<>(null);
        java.util.concurrent.atomic.AtomicBoolean hasSentChunks = new java.util.concurrent.atomic.AtomicBoolean(false);
        
        chatService.chatStream(sessionId, content)
            .subscribe(
                chunk -> {
                    try {
                        if (chunk != null && !chunk.isEmpty()) {
                            hasSentChunks.set(true);
                        }
                        if (agentModeRef.get() == null) {
                            agentModeRef.set(chatService.getSessionAgentMode(finalSessionId));
                        }
                        String agentMode = agentModeRef.get();
                        
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("sessionId", finalSessionId);
                        response.put("role", "ai");
                        response.put("content", chunk);
                        response.put("agentMode", agentMode);
                        response.put("isDelta", true);
                        synchronized(session) {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error sending chunk", ex);
                    }
                },
                error -> {
                    try {
                        if (agentModeRef.get() == null) {
                            agentModeRef.set(chatService.getSessionAgentMode(finalSessionId));
                        }
                        String agentMode = agentModeRef.get();
                        
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("sessionId", finalSessionId);
                        response.put("role", "ai");
                        response.put("content", "\n[Error: " + error.getMessage() + "]");
                        response.put("agentMode", agentMode);
                        response.put("isFinal", true);
                        synchronized(session) {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error sending error", ex);
                    }
                },
                () -> {
                    try {
                        if (agentModeRef.get() == null) {
                            agentModeRef.set(chatService.getSessionAgentMode(finalSessionId));
                        }
                        String agentMode = agentModeRef.get();
                        
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("sessionId", finalSessionId);
                        response.put("role", "ai");
                        
                        if (!hasSentChunks.get()) {
                            log.warn("Stream completed without sending any chunks for session: {}", finalSessionId);
                            response.put("content", "很抱歉，AI 未回傳任何內容，這可能是因為模型仍在處理工具呼叫或發生了預期外的中斷。請再次輸入「OK」重試。");
                        } else {
                            response.put("content", ""); // Empty content for the final signal
                        }
                        
                        response.put("agentMode", agentMode);
                        response.put("isFinal", true);
                        synchronized(session) {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error sending complete", ex);
                    }
                }
            );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", session.getId());
    }
}
