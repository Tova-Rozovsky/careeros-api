package com.careeros.repository;
import com.careeros.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    List<JobApplication> findByUserOrderByCreatedAtDesc(User user);
    List<JobApplication> findByUserAndStatus(User user, JobApplication.ApplicationStatus status);
}