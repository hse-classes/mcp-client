package kmo.dev;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatMemory chatMemory() {
        // In-memory, keyed by HTTP session: gone after a restart, which is fine for a demo.
        return MessageWindowChatMemory.builder().maxMessages(20).build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
        // No tools here: the MCP tools are attached per request in ChatController,
        // so each answer can show which tools the model actually called.
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        Du bist ein hilfreicher Assistent für eine Hochschul-Verwaltung.
                        Nutze die verfügbaren Tools, um Fragen zu Studenten zu beantworten.
                        Antworte kurz und auf Deutsch.
                        """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
