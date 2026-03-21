package com.github.miyohide.mymcp.session;

import com.github.miyohide.mymcp.model.ChatMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@SessionScope
public class ChatSession {

    private final List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(String userMessage, String botResponse) {
        messages.add(new ChatMessage(userMessage, botResponse, LocalDateTime.now(), false));
    }

    public void addErrorMessage(String userMessage, String errorMessage) {
        messages.add(new ChatMessage(userMessage, errorMessage, LocalDateTime.now(), true));
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void clear() {
        messages.clear();
    }
}
