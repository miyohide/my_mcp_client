package com.github.miyohide.mymcp.service;

import com.github.miyohide.mymcp.exception.McpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

/**
 * Spring AI MCP クライアントスターターと Amazon Bedrock Converse API を使用して
 * MCPサーバーのツール発見・選択・呼び出し・応答解析を行うサービス。
 *
 * <p>処理フロー:
 * <ol>
 *   <li>spring-ai-starter-mcp-client が起動時に MCP サーバーへ接続し tools/list を実行</li>
 *   <li>発見したツールを ToolCallback として ChatClient に登録</li>
 *   <li>ユーザーメッセージを Bedrock LLM に送信し、LLM がツールを選択</li>
 *   <li>Spring AI が tools/call を実行し、結果を LLM に返して最終応答を生成</li>
 * </ol>
 */
@Service
public class McpClientService {

    private static final Logger log = LoggerFactory.getLogger(McpClientService.class);

    private final ChatClient chatClient;

    public McpClientService(ChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        // MCP サーバーから発見したツールを ChatClient に登録
        ToolCallback[] tools = toolCallbackProvider.getToolCallbacks();
        this.chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(tools)
                .build();
        log.info("MCPクライアントサービスを初期化しました。利用可能なツール数: {}",
                toolCallbackProvider.getToolCallbacks().length);
    }

    /**
     * ユーザーメッセージを Bedrock LLM に送信する。
     * LLM が必要と判断した場合、MCP サーバーのツールを自動的に呼び出す。
     *
     * @param message ユーザーからの入力メッセージ
     * @return LLM が生成した最終応答テキスト
     * @throws McpException ツール呼び出しや応答生成に失敗した場合
     */
    public String sendMessage(String message) {
        try {
            log.debug("メッセージを送信します: {}", message);
            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
            log.debug("応答を受信しました");
            return response;
        } catch (Exception e) {
            log.error("メッセージ送信中にエラーが発生しました", e);
            throw new McpException("MCPサーバーとの通信に失敗しました: " + e.getMessage(), e);
        }
    }
}
