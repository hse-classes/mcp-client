package kmo.dev;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

/** The chat history lives in memory, so it is dropped as soon as its HTTP session ends (logout or timeout). */
@Component
public class ChatMemoryCleanup implements HttpSessionListener {

    private final ChatMemory chatMemory;

    public ChatMemoryCleanup(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        chatMemory.clear(event.getSession().getId());
    }
}
