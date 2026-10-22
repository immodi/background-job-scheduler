package com.projects.backgroundjobscheduler.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateJobRequest(
        @NotBlank String name,
        String description,
        @NotNull @Future LocalDateTime scheduledAt
) {}
