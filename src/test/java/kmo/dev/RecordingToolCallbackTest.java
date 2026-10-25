package kmo.dev;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

class RecordingToolCallbackTest {

    @Test
    void recordsNameArgumentsAndResult() {
        ToolCallback stub = new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                return ToolDefinition.builder().name("studentenStatistik").description("d").inputSchema("{}").build();
            }

            @Override
            public String call(String toolInput) {
                return "6 Studenten";
            }
        };
        var calls = new ArrayList<RecordingToolCallback.ToolCall>();

        String result = new RecordingToolCallback(stub, calls).call("{}");

        assertEquals("6 Studenten", result);
        assertEquals(1, calls.size());
        assertEquals("studentenStatistik", calls.get(0).tool());
        assertEquals("{}", calls.get(0).arguments());
        assertEquals("6 Studenten", calls.get(0).result());
    }
}
