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
    }

    if (messageForm) {
        messageForm.addEventListener('submit', handleFormSubmit);
    }
})();
