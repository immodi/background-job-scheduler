package com.projects.backgroundjobscheduler.service;

import com.projects.backgroundjobscheduler.entity.Job;
import com.projects.backgroundjobscheduler.enums.TaskType;
import com.projects.backgroundjobscheduler.exception.InvalidJobStateException;
import com.projects.backgroundjobscheduler.exception.JobNotFoundException;
import com.projects.backgroundjobscheduler.task.ITask;
import com.projects.backgroundjobscheduler.task.ITaskResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobWorkerTest {
    @Mock
    private JobService jobService;

    @Mock
    private ITaskResolver resolver;

    @Mock
    private ITask task;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JobWorker worker;
    private Job job;

    @BeforeEach
    void setUp() {
        worker = new JobWorker(jobService, resolver);

        job = new Job("test job", "test job description", LocalDateTime.now().plusHours(1));
        job.setTaskType(TaskType.GENERATE_REPORT);
    }

    @Test
    void execute_startsResolvesAndCompletesJob() {
        JsonNode parameters = objectMapper.createObjectNode().put("topic", "Q4 Sales");
        JsonNode output = objectMapper.createObjectNode().put("body", "done");
        job.setParameters(parameters);

        when(jobService.getJobOrThrow(1L)).thenReturn(job);
        when(resolver.resolve(TaskType.GENERATE_REPORT)).thenReturn(task);
        when(task.execute(parameters)).thenReturn(output);

        worker.execute(1L);

        InOrder order = inOrder(jobService, resolver, task);
        order.verify(jobService).startJob(1L);
        order.verify(jobService).getJobOrThrow(1L);
        order.verify(resolver).resolve(TaskType.GENERATE_REPORT);
        order.verify(task).execute(parameters);
        order.verify(jobService).completeJob(1L, output);

        verify(jobService, never()).failJob(anyLong());
    }

    @Test
    void execute_marksJobFailedWhenTaskThrows() {
        when(jobService.getJobOrThrow(1L)).thenReturn(job);
        when(resolver.resolve(TaskType.GENERATE_REPORT)).thenReturn(task);
        when(task.execute(any())).thenThrow(new IllegalStateException("boom"));

        worker.execute(1L);

        verify(jobService).failJob(1L);
        verify(jobService, never()).completeJob(anyLong(), any());
    }

    @Test
    void execute_marksJobFailedWhenCompletionThrows() {
        JsonNode output = objectMapper.createObjectNode();

        when(jobService.getJobOrThrow(1L)).thenReturn(job);
        when(resolver.resolve(TaskType.GENERATE_REPORT)).thenReturn(task);
        when(task.execute(any())).thenReturn(output);
        doThrow(new InvalidJobStateException("Only running jobs can be completed"))
                .when(jobService).completeJob(1L, output);

        worker.execute(1L);

        verify(jobService).failJob(1L);
    }

    @Test
    void execute_doesNotRunTaskWhenJobCannotBeStarted() {
        doThrow(new InvalidJobStateException("Only pending jobs can be started"))
                .when(jobService).startJob(1L);

        worker.execute(1L);

        verifyNoInteractions(resolver);
        verify(jobService, never()).getJobOrThrow(anyLong());
        verify(jobService, never()).completeJob(anyLong(), any());
        verify(jobService, never()).failJob(anyLong());
    }

    @Test
    void execute_doesNotMarkFailedWhenJobIsMissing() {
        doThrow(new JobNotFoundException(1L)).when(jobService).startJob(1L);

        worker.execute(1L);

        verifyNoInteractions(resolver);
        verify(jobService, never()).completeJob(eq(1L), any());
        verify(jobService, never()).failJob(anyLong());
    }
}
