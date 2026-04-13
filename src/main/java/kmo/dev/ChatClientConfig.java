package kmo.dev;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel, List<McpSyncClient> mcpClients) {
        final var toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpClients);
        return ChatClient.builder(chatModel).defaultSystem("""
                                                                     Du bist ein hilfreicher Assistent für eine Hochschul-Verwaltung.
                                                                     ntworte mit den Ergebnissen auf Deutsch.
                        """)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }
}