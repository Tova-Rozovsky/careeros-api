package com.careeros.repository;
import com.careeros.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    List<Resume> findByUserOrderByCreatedAtDesc(User user);
    List<Resume> findByUserAndIsActiveTrue(User user);
}