
package com.projects.backgroundjobscheduler.controller;

import com.projects.backgroundjobscheduler.dto.CreateJobRequest;
import com.projects.backgroundjobscheduler.dto.UpdateJobRequest;
import com.projects.backgroundjobscheduler.entity.Job;
import com.projects.backgroundjobscheduler.exception.GlobalExceptionHandler;
import com.projects.backgroundjobscheduler.exception.InvalidJobStateException;
import com.projects.backgroundjobscheduler.exception.JobNotFoundException;
import com.projects.backgroundjobscheduler.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
@Import(GlobalExceptionHandler.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobService jobService;

    @Test
    void createJob_returnsCreated() throws Exception {
        var request = new CreateJobRequest(
                "Test job",
                "Description",
                LocalDateTime.now().plusHours(1)
        );

        when(jobService.createJob(any(CreateJobRequest.class)))
                .thenAnswer(invocation -> new Job(
                        request.name(),
                        request.description(),
                        request.scheduledAt()
                ));

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test job"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createJob_withInvalidRequest_returnsBadRequest() throws Exception {
        var request = new CreateJobRequest(
                "",
                "Description",
                LocalDateTime.now().minusHours(1)
        );

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jobService);
    }

    @Test
    void getJobs_returnsList() throws Exception {
        when(jobService.getJobs()).thenReturn(List.of(
                new Job("Test job", "Description",
                        LocalDateTime.now().plusHours(1))
        ));

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test job"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getJob_whenMissing_returnsNotFound() throws Exception {
        when(jobService.getJobOrThrow(1L))
                .thenThrow(new JobNotFoundException(1L));

        mockMvc.perform(get("/jobs/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateJob_returnsUpdatedJob() throws Exception {
        var scheduledAt = LocalDateTime.now().plusHours(2);
        var request = new UpdateJobRequest(
                "Updated job", "Updated description", scheduledAt
        );

        when(jobService.updateJob(eq(1L), any(UpdateJobRequest.class)))
                .thenReturn(new Job(
                        request.name(),
                        request.description(),
                        request.scheduledAt()
                ));

        mockMvc.perform(put("/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated job"));
    }

    @Test
    void updateJob_whenStateIsInvalid_returnsBadRequest() throws Exception {
        var request = new UpdateJobRequest(
                "Updated job",
                "Description",
                LocalDateTime.now().plusHours(2)
        );

        when(jobService.updateJob(eq(1L), any(UpdateJobRequest.class)))
                .thenThrow(new InvalidJobStateException(
                        "Job is not in PENDING state"
                ));

        mockMvc.perform(put("/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteJob_returnsNoContent() throws Exception {
        doNothing().when(jobService).deleteJob(1L);

        mockMvc.perform(delete("/jobs/1"))
                .andExpect(status().isNoContent());

        verify(jobService).deleteJob(1L);
    }

    @Test
    void cancelJob_returnsSuccessfulResponse() throws Exception {
        doNothing().when(jobService).cancelJob(1L);

        mockMvc.perform(post("/jobs/1/cancel"))
                .andExpect(status().isOk());

        verify(jobService).cancelJob(1L);
    }

    @Test
    void cancelJob_whenStateIsInvalid_returnsBadRequest() throws Exception {
        doThrow(new InvalidJobStateException(
                "Only pending jobs can be cancelled"
        )).when(jobService).cancelJob(1L);

        mockMvc.perform(post("/jobs/1/cancel"))
                .andExpect(status().isBadRequest());
    }
}
