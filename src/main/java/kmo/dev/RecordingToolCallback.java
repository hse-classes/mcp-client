package kmo.dev;

import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * Wraps a tool (here: a tool living on the MCP server) and notes every call,
 * so the page can show what the model asked the server to do.
 */
public class RecordingToolCallback implements ToolCallback {

    public record ToolCall(String tool, String arguments, String result) {
    }

    private final ToolCallback delegate;
    private final List<ToolCall> calls;

    public RecordingToolCallback(ToolCallback delegate, List<ToolCall> calls) {
        this.delegate = delegate;
        this.calls = calls;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public String call(String toolInput) {
        return record(toolInput, delegate.call(toolInput));
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        return record(toolInput, delegate.call(toolInput, toolContext));
    }

    private String record(String input, String result) {
        calls.add(new ToolCall(delegate.getToolDefinition().name(), input, result));
        return result;
    }
}
