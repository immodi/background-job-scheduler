package com.projects.backgroundjobscheduler.service;

import com.projects.backgroundjobscheduler.dto.CreateJobRequest;
import com.projects.backgroundjobscheduler.entity.Job;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.ZoneOffset;

@Service
public class JobOrchestrator {
    private final JobService jobService;
    private final JobScheduler jobScheduler;

    public JobOrchestrator(JobService jobService, JobScheduler jobScheduler) {
        this.jobService = jobService;
        this.jobScheduler = jobScheduler;
    }

    @Transactional
    public Job createJob(CreateJobRequest request) {
        Job savedJob = jobService.createJob(request);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        jobScheduler.schedule(
                                savedJob.getId(),
                                savedJob.getScheduledAt().toInstant(ZoneOffset.UTC)
                        );
                    }
                }
        );

        return savedJob;
    }
}
