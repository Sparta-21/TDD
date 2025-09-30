package com.sparta.tdd.domain.ai.repository;

import com.sparta.tdd.domain.ai.entity.Ai;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRepository extends JpaRepository<Ai, UUID> {

}
