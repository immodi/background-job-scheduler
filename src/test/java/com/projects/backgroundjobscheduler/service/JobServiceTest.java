package com.projects.backgroundjobscheduler.service;

import com.projects.backgroundjobscheduler.dto.CreateJobRequest;
import com.projects.backgroundjobscheduler.dto.UpdateJobRequest;
import com.projects.backgroundjobscheduler.entity.Job;
import com.projects.backgroundjobscheduler.enums.JobStatus;
import com.projects.backgroundjobscheduler.exception.InvalidJobStateException;
import com.projects.backgroundjobscheduler.exception.JobNotFoundException;
import com.projects.backgroundjobscheduler.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest {
    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    private Job job;

    @BeforeEach
    void setUp() {
        var scheduledAt = LocalDateTime.now().plusHours(1);
        job = new Job("test job", "test job description", scheduledAt);
    }

    @Test
    void createJob_savesJobToRepository() {
        var scheduledAt = LocalDateTime.now().plusHours(1);
        var request = new CreateJobRequest("Test job", "Description", scheduledAt);

        when(jobRepository.save(any(Job.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Job result = jobService.createJob(request);

        assertEquals("Test job", result.getName());
        assertEquals("Description", result.getDescription());
        assertEquals(scheduledAt, result.getScheduledAt());

        verify(jobRepository).save(any(Job.class));
    }


    @Test
    void getJobOrThrow_returnsJobWhenFound() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        Job result = jobService.getJobOrThrow(1L);

        assertEquals(job, result);
    }

    @Test
    void getJobOrThrow_throwsWhenJobNotFound() {
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> jobService.getJobOrThrow(1L));
    }

    @Test
    void getJobs_returnsAllJobs() {
        when(jobRepository.findAll()).thenReturn(List.of(job));

        List<Job> result = jobService.getJobs();

        assertEquals(1, result.size());
        assertEquals(job, result.get(0));
    }

    @Test
    void updateJob_updatesPendingJob() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        var scheduledAt = LocalDateTime.now().plusHours(2);
        var request = new UpdateJobRequest("Updated job", "Updated description", scheduledAt);

        Job result = jobService.updateJob(1L, request);

        assertEquals("Updated job", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(scheduledAt, result.getScheduledAt());
    }

    @Test
    void updateJob_throwsWhenJobIsNotPending() {
        job.markAsRunning();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        var request = new UpdateJobRequest(
                "Updated job", "Updated description",
                LocalDateTime.now().plusHours(2));

        assertThrows(InvalidJobStateException.class,
                () -> jobService.updateJob(1L, request));
    }

    @Test
    void deleteJob_deletesExistingJob() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        jobService.deleteJob(1L);

        verify(jobRepository).delete(job);
    }

    @Test
    void deleteJob_throwsWhenJobNotFound() {
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class,
                () -> jobService.deleteJob(1L));

        verify(jobRepository, never()).delete(any(Job.class));
    }

    @Test
    void cancelJob_cancelsPendingJob() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        jobService.cancelJob(1L);

        assertEquals(JobStatus.CANCELLED, job.getStatus());
    }

    @Test
    void cancelJob_throwsWhenJobIsNotPending() {
        job.markAsRunning();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class,
                () -> jobService.cancelJob(1L));
    }

    @Test
    void startJob_startsPendingJob() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        jobService.startJob(1L);

        assertEquals(JobStatus.RUNNING, job.getStatus());
    }

    @Test
    void startJob_throwsWhenJobIsNotPending() {
        job.markAsCompleted();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class,
                () -> jobService.startJob(1L));
    }

    @Test
    void completeJob_completesRunningJob() {
        job.markAsRunning();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        jobService.completeJob(1L);

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertNotNull(job.getCompletedAt());
    }

    @Test
    void completeJob_throwsWhenJobIsNotRunning() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class,
                () -> jobService.completeJob(1L));
    }

    @Test
    void failJob_marksRunningJobAsFailed() {
        job.markAsRunning();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        jobService.failJob(1L);

        assertEquals(JobStatus.FAILED, job.getStatus());
        assertNotNull(job.getCompletedAt());
    }

    @Test
    void failJob_throwsWhenJobIsNotRunning() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class,
                () -> jobService.failJob(1L));
    }



}
