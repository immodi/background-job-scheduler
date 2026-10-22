package com.projects.backgroundjobscheduler.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JobScheduler {
    private final ThreadPoolTaskScheduler scheduler;
    private final JobWorker worker;

    public JobScheduler(ThreadPoolTaskScheduler taskScheduler, JobWorker jobWorker) {
        this.scheduler = taskScheduler;
        this.worker = jobWorker;
    }

    public void schedule(Long jobId, Instant time) {
        scheduler.schedule(() -> worker.execute(jobId), time);
    }
}
