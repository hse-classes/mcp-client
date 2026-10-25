package kmo.dev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpSession;
import kmo.dev.RecordingToolCallback.ToolCall;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ChatController {

    static final int MAX_MESSAGE_LENGTH = 1000;

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String answerHtml, List<ToolCall> toolCalls) {
    }

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpTools;
    private final MarkdownRenderer markdown;
    // Sessions with a question still being answered. The chat memory is not safe for
    // two overlapping turns of one conversation (e.g. two tabs), so a second one is refused.
    private final Set<String> busySessions = ConcurrentHashMap.newKeySet();

    public ChatController(ChatClient chatClient, ToolCallbackProvider mcpTools, MarkdownRenderer markdown) {
        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
        this.markdown = markdown;
    }

    @GetMapping("/")
    public String chat() {
        return "chat";
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public ChatResponse ask(@RequestBody ChatRequest request, HttpSession session) {
        String message = request.message() == null ? "" : request.message().trim();
        if (message.isEmpty() || message.length() > MAX_MESSAGE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nachricht muss zwischen 1 und " + MAX_MESSAGE_LENGTH + " Zeichen lang sein.");
        }

        if (!busySessions.add(session.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die vorherige Frage wird noch beantwortet.");
        }
        try {
            List<ToolCall> calls = Collections.synchronizedList(new ArrayList<>());
            ToolCallback[] tools = Arrays.stream(mcpTools.getToolCallbacks())
                    .map(tool -> (ToolCallback) new RecordingToolCallback(tool, calls))
                    .toArray(ToolCallback[]::new);

            String answer = chatClient.prompt()
                    .user(message)
                    .toolCallbacks(tools)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, session.getId()))
                    .call()
                    .content();

            return new ChatResponse(markdown.toHtml(answer), List.copyOf(calls));
        } finally {
            busySessions.remove(session.getId());
        }
    }
}
