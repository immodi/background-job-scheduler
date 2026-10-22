package com.projects.backgroundjobscheduler.dto;

import com.projects.backgroundjobscheduler.enums.TaskType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record CreateJobRequest(
        @NotBlank String name,
        String description,
        @NotNull @Future LocalDateTime scheduledAt,
        @NotNull TaskType taskType,
        @NotNull JsonNode parameters
) {}