package com.github.miyohide.mymcp.controller;

import com.github.miyohide.mymcp.exception.McpConnectionException;
import com.github.miyohide.mymcp.exception.McpException;
import com.github.miyohide.mymcp.exception.McpTimeoutException;
import com.github.miyohide.mymcp.service.McpClientService;
import com.github.miyohide.mymcp.session.ChatSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final McpClientService mcpClientService;
    private final ChatSession chatSession;

    public ChatController(McpClientService mcpClientService, ChatSession chatSession) {
        this.mcpClientService = mcpClientService;
        this.chatSession = chatSession;
    }

    @GetMapping("/chat")
    public String showChat(Model model) {
        model.addAttribute("messages", chatSession.getMessages());
        return "chat";
    }

    @PostMapping("/chat")
    public String sendMessage(@RequestParam String message, Model model) {
        if (message == null || message.trim().isEmpty()) {
            model.addAttribute("messages", chatSession.getMessages());
            return "chat";
        }

        try {
            String response = mcpClientService.sendMessage(message);
            chatSession.addMessage(message, response);
        } catch (McpTimeoutException e) {
            log.error("MCPサーバーへの接続がタイムアウトしました", e);
            chatSession.addErrorMessage(message, "MCPサーバーへの接続がタイムアウトしました");
        } catch (McpConnectionException e) {
            log.error("MCPサーバーへの接続に失敗しました", e);
            chatSession.addErrorMessage(message, "MCPサーバーに接続できません。設定を確認してください。");
        } catch (McpException e) {
            log.error("MCPサーバーからエラーが返されました", e);
            chatSession.addErrorMessage(message, "MCPサーバーからエラーが返されました: " + e.getMessage());
        } catch (Exception e) {
            log.error("予期しないエラーが発生しました", e);
            chatSession.addErrorMessage(message, "予期しないエラーが発生しました");
        }

        model.addAttribute("messages", chatSession.getMessages());
        return "chat";
    }

    @PostMapping("/chat/clear")
    public String clearHistory() {
        chatSession.clear();
        return "redirect:/chat";
    }
}
