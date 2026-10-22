package com.projects.backgroundjobscheduler.entity;


import com.projects.backgroundjobscheduler.enums.JobStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    protected Job() {}
    public Job(String name, String description, LocalDateTime scheduledAt) {
        this.name = name;
        this.description = description;
        this.scheduledAt = scheduledAt;
        this.createdAt = LocalDateTime.now();
        this.status = JobStatus.PENDING;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    public void markAsRunning() {
        this.status = JobStatus.RUNNING;
    }

    public void markAsCompleted() {
        this.status = JobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = JobStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = JobStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsPending() {
        this.status = JobStatus.PENDING;
    }

    public JobStatus getStatus() {
        return status;
    }
}
