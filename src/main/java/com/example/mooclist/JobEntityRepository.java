package com.example.mooclist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface JobEntityRepository extends JpaRepository<JobEntity, String> {
    List<JobEntity> findAll();
    List<JobEntity> findAllById(Integer id);
}
