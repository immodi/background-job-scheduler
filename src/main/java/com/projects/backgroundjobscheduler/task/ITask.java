package com.projects.backgroundjobscheduler.task;

import com.projects.backgroundjobscheduler.enums.TaskType;
import tools.jackson.databind.JsonNode;

public interface ITask {
    TaskType getType();
    JsonNode execute(JsonNode parameters);
}
