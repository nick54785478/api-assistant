package com.apiassistant.agent.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionToolProvider {

    private final RestTemplate restTemplate;

    @Cacheable(value = "sessionTools", key = "#sessionId", unless = "#result == null")
    public List<String> getSessionToolNames(String sessionId) {
        log.info("Fetching allowed tools for session {} from Express API", sessionId);
        List<String> allowedToolNames = new ArrayList<>();
        try {
            String url = "http://localhost:3001/tools?sessionId=" + sessionId;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tools = restTemplate.getForObject(url, List.class);
            if (tools != null) {
                for (Map<String, Object> t : tools) {
                    String name = (String) t.get("name");
                    String tSessionId = (String) t.get("session_id");
                    if (tSessionId != null && !tSessionId.isEmpty()) {
                        String cleanSessionId = tSessionId.replaceAll("[^a-zA-Z0-9_]", "");
                        if (cleanSessionId.length() > 4) {
                            cleanSessionId = cleanSessionId.substring(0, 4);
                        }
                        name = "s_" + cleanSessionId + "_" + name;
                    }
                    allowedToolNames.add(name);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch session tools, defaulting to empty or global tools", e);
        }
        return allowedToolNames;
    }
}
