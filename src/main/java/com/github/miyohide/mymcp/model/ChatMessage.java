package com.github.miyohide.mymcp.model;

import java.time.LocalDateTime;

public record ChatMessage(
        String userMessage,
        String botResponse,
        LocalDateTime timestamp,
        boolean isError
) {}
