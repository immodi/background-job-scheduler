package com.projects.backgroundjobscheduler.service;

import com.projects.backgroundjobscheduler.entity.Job;
import com.projects.backgroundjobscheduler.exception.InvalidJobStateException;
import com.projects.backgroundjobscheduler.exception.JobNotFoundException;
import com.projects.backgroundjobscheduler.task.ITask;
import com.projects.backgroundjobscheduler.task.ITaskResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class JobWorker {
    private final ITaskResolver resolver;
    private final JobService service;
    private static final Logger log = LoggerFactory.getLogger(JobWorker.class);


    public JobWorker(JobService service, ITaskResolver resolver) {
        this.service = service;
        this.resolver = resolver;
    }

    public void execute(Long jobId) {
        try {
            service.startJob(jobId);
        } catch (JobNotFoundException ex) {
            log.error("Failed to find job with id: {}", jobId, ex);
            return;
        } catch (InvalidJobStateException ex) {
            log.warn("Skipping job {} because it cannot be started", jobId);
            return;
        }

        try {
            Job job = service.getJobOrThrow(jobId);
            ITask task = resolver.resolve(job.getTaskType());
            JsonNode result = task.execute(job.getParameters());

            service.completeJob(jobId, result);
            log.info("Successfully completed job {}", jobId);
        } catch (Exception ex) {
            log.error("Failed to execute job with id: {}", jobId, ex);

            try {
                service.failJob(jobId);
            } catch (Exception failureEx) {
                log.error("Failed to mark job {} as FAILED", jobId, failureEx);
            }
        }
    }
}
