package com.projects.backgroundjobscheduler.service;

import com.projects.backgroundjobscheduler.dto.CreateJobRequest;
import com.projects.backgroundjobscheduler.dto.UpdateJobRequest;
import com.projects.backgroundjobscheduler.entity.Job;
import com.projects.backgroundjobscheduler.enums.JobStatus;
import com.projects.backgroundjobscheduler.exception.InvalidJobStateException;
import com.projects.backgroundjobscheduler.exception.JobNotFoundException;
import com.projects.backgroundjobscheduler.repository.JobRepository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job getJobOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    @Transactional
    public Job createJob(CreateJobRequest request) {
        return jobRepository.save(new Job(request.name(), request.description(), request.scheduledAt()));
    }

    @Transactional
    // PUT style full update
    public Job updateJob(Long id, UpdateJobRequest request) {
        Job job = getJobOrThrow(id);

        if (job.getStatus() != JobStatus.PENDING) {
            throw new InvalidJobStateException("Job is not in PENDING state");
        }

        job.setName(request.name());
        job.setDescription(request.description());
        job.setScheduledAt(request.scheduledAt());

        return job;
    }

    @Transactional
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Job> getJobs() {
        return jobRepository.findAll();
    }

    @Transactional
    public void cancelJob(Long id) {
        Job job = getJobOrThrow(id);

        if (job.getStatus() != JobStatus.PENDING) {
            throw new InvalidJobStateException("Only pending jobs can be cancelled");
        }

        job.markAsCancelled();
    }

    @Transactional
    public void startJob(Long id) {
        Job job = getJobOrThrow(id);

        if (job.getStatus() != JobStatus.PENDING) {
            throw new InvalidJobStateException("Only pending jobs can be started");
        }

        job.markAsRunning();
    }

    @Transactional
    public void completeJob(Long id) {
        Job job = getJobOrThrow(id);

        if (job.getStatus() != JobStatus.RUNNING) {
            throw new InvalidJobStateException("Only running jobs can be completed");
        }

        job.markAsCompleted();
    }

    @Transactional
    public void failJob(Long id) {
        Job job = getJobOrThrow(id);

        if (job.getStatus() != JobStatus.RUNNING) {
            throw new InvalidJobStateException("Only running jobs can fail");
        }

        job.markAsFailed();
    }
}
