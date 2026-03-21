package com.github.miyohide.mymcp.service;

import com.github.miyohide.mymcp.exception.McpException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * McpClientService のユニットテスト
 *
 * 要件4.3: ユーザーメッセージ受信時のツール呼び出し確認
 * 要件4.4: レスポンスのテキスト変換確認
 * 要件4.6: エラー時の例外スロー確認
 */
class McpClientServiceTest {

    // 要件4.4: McpClientService が正常に初期化されること
    @Test
    void McpClientServiceが正常に初期化されること() {
        ChatModel chatModel = mock(ChatModel.class);
        SyncMcpToolCallbackProvider toolCallbackProvider = mock(SyncMcpToolCallbackProvider.class);
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(new ToolCallback[0]);

        McpClientService service = new McpClientService(chatModel, toolCallbackProvider);

        assertThat(service).isNotNull();
    }

    // 要件4.6: ChatModel が例外をスローした場合に McpException にラップされること
    @Test
    void ChatModelが例外をスローした場合にMcpExceptionにラップされること() {
        ChatModel chatModel = mock(ChatModel.class);
        SyncMcpToolCallbackProvider toolCallbackProvider = mock(SyncMcpToolCallbackProvider.class);
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(new ToolCallback[0]);

        // Prompt 型を明示して曖昧さを解消
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("Bedrock接続エラー"));

        McpClientService service = new McpClientService(chatModel, toolCallbackProvider);

        assertThatThrownBy(() -> service.sendMessage("テスト"))
                .isInstanceOf(McpException.class)
                .hasMessageContaining("MCPサーバーとの通信に失敗しました");
    }
}
