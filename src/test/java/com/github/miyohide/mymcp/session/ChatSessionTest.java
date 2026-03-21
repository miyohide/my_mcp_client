package com.github.miyohide.mymcp.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatSessionTest {

    private ChatSession chatSession;

    @BeforeEach
    void setUp() {
        chatSession = new ChatSession();
    }

    // 要件6.5: clear() 後にリストが空になることを確認
    @Test
    void clear後にメッセージリストが空になること() {
        chatSession.addMessage("こんにちは", "こんにちは！何かお手伝いできますか？");
        chatSession.addMessage("今日の天気は？", "晴れです。");
        chatSession.addErrorMessage("エラーテスト", "接続エラーが発生しました。");

        chatSession.clear();

        assertThat(chatSession.getMessages()).isEmpty();
    }

    // 要件6.5: メッセージが0件の状態でclear()を呼んでも問題ないこと
    @Test
    void 空の状態でclearを呼んでも例外が発生しないこと() {
        chatSession.clear();

        assertThat(chatSession.getMessages()).isEmpty();
    }

    // 要件6.1: 複数セッションの独立性確認
    @Test
    void 別のChatSessionインスタンスは独立した履歴を持つこと() {
        ChatSession session1 = new ChatSession();
        ChatSession session2 = new ChatSession();

        session1.addMessage("セッション1のメッセージ", "セッション1の応答");

        assertThat(session1.getMessages()).hasSize(1);
        assertThat(session2.getMessages()).isEmpty();
    }

    // 要件6.1: 一方のセッションをクリアしても他方に影響しないこと
    @Test
    void 一方のセッションをクリアしても他方に影響しないこと() {
        ChatSession session1 = new ChatSession();
        ChatSession session2 = new ChatSession();

        session1.addMessage("セッション1のメッセージ", "セッション1の応答");
        session2.addMessage("セッション2のメッセージ", "セッション2の応答");

        session1.clear();

        assertThat(session1.getMessages()).isEmpty();
        assertThat(session2.getMessages()).hasSize(1);
    }
}
