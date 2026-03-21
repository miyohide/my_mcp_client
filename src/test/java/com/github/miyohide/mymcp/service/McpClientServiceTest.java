package com.github.miyohide.mymcp.service;

import com.github.miyohide.mymcp.config.McpConfig;
import com.github.miyohide.mymcp.exception.McpConnectionException;
import com.github.miyohide.mymcp.exception.McpException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * McpClientService のユニットテスト
 *
 * 要件4.2: 起動時のinitializeリクエスト送信確認
 * 要件4.5: 接続失敗時の例外スロー確認
 * 要件4.6: エラーレスポンス時の例外スロー確認
 */
class McpClientServiceTest {

    private WireMockServer wireMock;
    private McpConfig mcpConfig;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        mcpConfig = new McpConfig();
        mcpConfig.setUrl("http://localhost:" + wireMock.port());
        mcpConfig.setConnectionTimeout(1000);
        mcpConfig.setReadTimeout(3000);

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    // 要件4.2: アプリ起動時（@PostConstruct）にinitializeリクエストが送信されること
    @Test
    void 起動時にinitializeリクエストが送信されること() {
        wireMock.stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{}}")));

        // initialize() は @PostConstruct で呼ばれるが、直接呼び出して検証する
        McpClientService service = new McpClientService(mcpConfig, objectMapper);
        service.initialize();

        wireMock.verify(postRequestedFor(urlEqualTo("/"))
                .withRequestBody(matchingJsonPath("$.method", equalTo("initialize")))
                .withRequestBody(matchingJsonPath("$.jsonrpc", equalTo("2.0")))
                .withRequestBody(matchingJsonPath("$.params.protocolVersion", equalTo("2024-11-05")))
                .withRequestBody(matchingJsonPath("$.params.clientInfo.name", equalTo("mymcp"))));
    }

    // 要件4.5 (エッジケース): MCPサーバーへの接続が失敗した場合にMcpConnectionExceptionがスローされること
    @Test
    void 接続失敗時にMcpConnectionExceptionがスローされること() {
        // 存在しないポートを指定して接続失敗を再現
        mcpConfig.setUrl("http://localhost:1");

        McpClientService service = new McpClientService(mcpConfig, objectMapper);

        assertThatThrownBy(() -> service.sendMessage("テスト"))
                .isInstanceOf(McpConnectionException.class);
    }

    // 要件4.6 (エッジケース): MCPサーバーがエラーレスポンスを返した場合にMcpExceptionがスローされること
    @Test
    void エラーレスポンス時にMcpExceptionがスローされること() {
        wireMock.stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"jsonrpc\":\"2.0\",\"id\":2,\"error\":{\"code\":-32601,\"message\":\"Method not found\"}}")));

        McpClientService service = new McpClientService(mcpConfig, objectMapper);

        assertThatThrownBy(() -> service.sendMessage("テスト"))
                .isInstanceOf(McpException.class)
                .hasMessageContaining("Method not found");
    }
}
