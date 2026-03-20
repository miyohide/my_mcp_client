package com.github.miyohide.mymcp.exception;

public class McpConnectionException extends McpException {

    public McpConnectionException(String message) {
        super(message);
    }

    public McpConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
