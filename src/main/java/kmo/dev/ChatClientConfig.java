package kmo.dev;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ToolCallbackProvider mcpTools) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        Du bist ein hilfreicher Assistent für eine Hochschul-Verwaltung.
                        Antworte mit den Ergebnissen auf Deutsch.
                        """)
                // Tools come from the MCP server(s) configured in application.yml,
                // not from this application.
                .defaultToolCallbacks(mcpTools)
                .build();
    }
}
