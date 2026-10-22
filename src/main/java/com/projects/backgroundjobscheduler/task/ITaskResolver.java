package com.projects.backgroundjobscheduler.task;

import com.projects.backgroundjobscheduler.enums.TaskType;

public interface ITaskResolver {
    ITask resolve(TaskType type);
}
