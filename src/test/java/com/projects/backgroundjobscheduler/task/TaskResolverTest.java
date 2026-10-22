package com.projects.backgroundjobscheduler.task;

import com.projects.backgroundjobscheduler.enums.TaskType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class TaskResolverTest {
    private final TaskResolver resolver = new TaskResolver(new ObjectMapper());

    @Test
    void resolve_returnsTaskForType() {
        ITask task = resolver.resolve(TaskType.GENERATE_REPORT);

        assertInstanceOf(GenerateReportTask.class, task);
        assertEquals(TaskType.GENERATE_REPORT, task.getType());
    }
}
