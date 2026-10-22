package com.projects.backgroundjobscheduler.task;

import com.projects.backgroundjobscheduler.enums.TaskType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenerateReportTaskTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void execute_returnsReportForTopic() {
        GenerateReportTask task = new GenerateReportTask(objectMapper);
        JsonNode parameters = objectMapper.createObjectNode().put("topic", "Q4 Sales");

        JsonNode result = task.execute(parameters);

        assertEquals("Q4 Sales", result.get("topic").asString());
        assertEquals("Report generated successfully", result.get("body").asString());
    }

    @Test
    void getType_returnsGenerateReport() {
        GenerateReportTask task = new GenerateReportTask(objectMapper);

        assertEquals(TaskType.GENERATE_REPORT, task.getType());
    }
}
