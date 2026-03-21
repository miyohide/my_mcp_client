(function () {
    'use strict';

    const chatHistory = document.getElementById('chat-history');
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    const messageForm = messageInput ? messageInput.closest('form') : null;

    // ページ読み込み時に最下部へスクロール
    function scrollToBottom() {
        if (chatHistory) {
            chatHistory.scrollTop = chatHistory.scrollHeight;
        }
    }

    // Enterキーでフォーム送信（Shift+Enterは無視）
    function handleKeyDown(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            if (messageForm && !sendButton.disabled) {
                sendButton.disabled = true;
                messageForm.submit();
            }
        }
    }

    // フォーム送信時に送信ボタンを無効化
    function handleFormSubmit() {
        if (sendButton) {
            sendButton.disabled = true;
        }
    }

    // 初期化
    scrollToBottom();

    if (messageInput) {
        messageInput.focus();
        messageInput.addEventListener('keydown', handleKeyDown);
    }

    if (messageForm) {
        messageForm.addEventListener('submit', handleFormSubmit);
    }
})();
