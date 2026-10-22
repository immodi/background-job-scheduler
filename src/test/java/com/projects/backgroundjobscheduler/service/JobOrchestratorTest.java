package com.projects.backgroundjobscheduler.service;

import com.projects.backgroundjobscheduler.dto.CreateJobRequest;
import com.projects.backgroundjobscheduler.entity.Job;
import com.projects.backgroundjobscheduler.enums.TaskType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobOrchestratorTest {
    @Mock
    private JobService jobService;

    @Mock
    private JobScheduler jobScheduler;

    private JobOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new JobOrchestrator(jobService, jobScheduler);
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void createJob_schedulesOnlyAfterCommit() {
        LocalDateTime scheduledAt = LocalDateTime.now().plusHours(1).withNano(0);
        CreateJobRequest request = new CreateJobRequest(
                "test job",
                "test job description",
                scheduledAt,
                TaskType.GENERATE_REPORT,
                new ObjectMapper().createObjectNode()
        );

        Job savedJob = new Job(request.name(), request.description(), request.scheduledAt());
        savedJob.setTaskType(request.taskType());
        savedJob.setParameters(request.parameters());
        ReflectionTestUtils.setField(savedJob, "id", 42L);

        when(jobService.createJob(request)).thenReturn(savedJob);

        Job result = orchestrator.createJob(request);

        assertSame(savedJob, result);
        verify(jobService).createJob(request);
        verifyNoInteractions(jobScheduler);

        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());

        synchronizations.get(0).afterCommit();

        verify(jobScheduler).schedule(
                42L,
                scheduledAt.toInstant(ZoneOffset.UTC)
        );
    }
}
