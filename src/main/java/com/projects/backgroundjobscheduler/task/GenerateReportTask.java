package com.projects.backgroundjobscheduler.task;

import com.projects.backgroundjobscheduler.dto.GenerateReportTaskResponse;
import com.projects.backgroundjobscheduler.enums.TaskType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class GenerateReportTask implements ITask {
    private final ObjectMapper mapper;
    public GenerateReportTask(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    @Override
    public TaskType getType() {
        return TaskType.GENERATE_REPORT;
    }

    @Override
    public JsonNode execute(JsonNode parameters) {
        String topic = parameters.get("topic").asString();
        return mapper.valueToTree(new GenerateReportTaskResponse(
                topic,
                "Report generated successfully"
        ));
    }
}