package com.mailai.repository;

import com.mailai.model.AiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



public interface AiAnalysisRepository
        extends JpaRepository<AiAnalysis, Long> {


    Page<AiAnalysis> findByUserId(Long userId, Pageable pageable);

}
