package com.github.miyohide.mymcp.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.github.miyohide.mymcp.config.McpConfig;
import com.github.miyohide.mymcp.exception.McpConnectionException;
import com.github.miyohide.mymcp.exception.McpException;
import com.github.miyohide.mymcp.exception.McpTimeoutException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class McpClientService {

    private static final Logger log = LoggerFactory.getLogger(McpClientService.class);

    private final McpConfig mcpConfig;
    private final ObjectMapper objectMapper;
    private final AtomicLong idCounter = new AtomicLong(1);

    public McpClientService(McpConfig mcpConfig, ObjectMapper objectMapper) {
        this.mcpConfig = mcpConfig;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initialize() {
        log.info("MCPサーバーへの初期化リクエストを送信します: {}", mcpConfig.getUrl());
        try {
            ObjectNode params = objectMapper.createObjectNode();
            params.put("protocolVersion", "2024-11-05");
            ObjectNode clientInfo = objectMapper.createObjectNode();
            clientInfo.put("name", "mymcp");
            clientInfo.put("version", "0.0.1");
            params.set("clientInfo", clientInfo);

            String requestBody = buildRequest("initialize", params, idCounter.getAndIncrement());
            sendRequest(requestBody);
            log.info("MCPサーバーへの初期化が完了しました");
        } catch (McpException e) {
            log.warn("MCPサーバーへの初期化に失敗しました（サーバーが起動していない可能性があります）: {}", e.getMessage());
        }
    }

    public String sendMessage(String message) {
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", "chat");
        ObjectNode arguments = objectMapper.createObjectNode();
        arguments.put("message", message);
        params.set("arguments", arguments);

        String requestBody = buildRequest("tools/call", params, idCounter.getAndIncrement());
        JsonNode response = sendRequest(requestBody);
        return extractText(response);
    }

    private String buildRequest(String method, ObjectNode params, long id) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("jsonrpc", "2.0");
            request.put("id", id);
            request.put("method", method);
            request.set("params", params);
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new McpException("JSONリクエストの構築に失敗しました", e);
        }
    }

    private JsonNode sendRequest(String requestBody) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(mcpConfig.getConnectionTimeout()))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mcpConfig.getUrl()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(mcpConfig.getReadTimeout()))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if (root.has("error")) {
                JsonNode error = root.get("error");
                String errorMessage = error.has("message") ? error.get("message").textValue() : "不明なエラー";
                throw new McpException("MCPサーバーからエラーが返されました: " + errorMessage);
            }

            return root;
        } catch (McpException e) {
            throw e;
        } catch (HttpTimeoutException e) {
            throw new McpTimeoutException("MCPサーバーへの接続がタイムアウトしました", e);
        } catch (ConnectException e) {
            throw new McpConnectionException("MCPサーバーに接続できません。設定を確認してください。", e);
        } catch (Exception e) {
            if (e.getCause() instanceof ConnectException ce) {
                throw new McpConnectionException("MCPサーバーに接続できません。設定を確認してください。", ce);
            }
            throw new McpConnectionException("MCPサーバーへの接続に失敗しました: " + e.getMessage(), e);
        }
    }

    private String extractText(JsonNode response) {
        try {
            JsonNode content = response.path("result").path("content");
            if (content.isMissingNode() || !content.isArray()) {
                throw new McpException("MCPサーバーのレスポンス形式が不正です");
            }
            StringBuilder sb = new StringBuilder();
            for (JsonNode item : content) {
                if ("text".equals(item.path("type").textValue())) {
                    sb.append(item.path("text").textValue());
                }
            }
            return sb.toString();
        } catch (McpException e) {
            throw e;
        } catch (Exception e) {
            throw new McpException("MCPサーバーのレスポンス解析に失敗しました", e);
        }
    }
}
