package com.github.miyohide.mymcp.controller;

import com.github.miyohide.mymcp.exception.McpConnectionException;
import com.github.miyohide.mymcp.exception.McpTimeoutException;
import com.github.miyohide.mymcp.service.McpClientService;
import com.github.miyohide.mymcp.session.ChatSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private McpClientService mcpClientService;

    @MockitoBean
    private ChatSession chatSession;

    // 要件1.1: GET /chat が HTTP 200 を返すこと
    @Test
    void GET_chatが200を返すこと() throws Exception {
        when(chatSession.getMessages()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk());
    }

    // 要件1.2: チャット画面にメッセージ入力フォームが含まれること
    @Test
    void チャット画面にformタグが含まれること() throws Exception {
        when(chatSession.getMessages()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<form")));
    }

    // 要件1.3: チャット画面に送信ボタンが含まれること
    @Test
    void チャット画面に送信ボタンが含まれること() throws Exception {
        when(chatSession.getMessages()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("送信")));
    }

    // 要件1.4: チャット画面に会話履歴エリアが含まれること
    @Test
    void チャット画面に会話履歴エリアが含まれること() throws Exception {
        when(chatSession.getMessages()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("chat-history")));
    }

    // 要件1.6: 空履歴時に空の会話履歴エリアが表示されること
    @Test
    void 空履歴時に空の会話履歴メッセージが表示されること() throws Exception {
        when(chatSession.getMessages()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("会話履歴はありません")));
    }

    // 要件5.2: McpTimeoutException スロー時にタイムアウトメッセージが表示されること
    @Test
    void タイムアウト例外時に特定メッセージが表示されること() throws Exception {
        when(mcpClientService.sendMessage(anyString()))
                .thenThrow(new McpTimeoutException("タイムアウト"));
        when(chatSession.getMessages()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/chat").param("message", "テスト"))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(chatSession)
                .addErrorMessage("テスト", "MCPサーバーへの接続がタイムアウトしました");
    }

    // 要件5.3: McpConnectionException スロー時に接続不可メッセージが表示されること
    @Test
    void 接続不可例外時に特定メッセージが表示されること() throws Exception {
        when(mcpClientService.sendMessage(anyString()))
                .thenThrow(new McpConnectionException("接続不可"));
        when(chatSession.getMessages()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/chat").param("message", "テスト"))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(chatSession)
                .addErrorMessage("テスト", "MCPサーバーに接続できません。設定を確認してください。");
    }
}
