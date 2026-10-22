package com.projects.backgroundjobscheduler.repository;

import com.projects.backgroundjobscheduler.entity.Job;
import com.projects.backgroundjobscheduler.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);
}
