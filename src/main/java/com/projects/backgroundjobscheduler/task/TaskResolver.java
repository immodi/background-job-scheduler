package com.projects.backgroundjobscheduler.task;

import com.projects.backgroundjobscheduler.enums.TaskType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;


@Component
public class TaskResolver implements ITaskResolver {
    private final ObjectMapper mapper;

    public TaskResolver(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ITask resolve(TaskType type) {
        return switch (type) {
            case GENERATE_REPORT -> new GenerateReportTask(mapper);
        };
    }
}
