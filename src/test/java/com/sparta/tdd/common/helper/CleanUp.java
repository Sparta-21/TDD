package com.sparta.tdd.common.helper;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CleanUp {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public CleanUp(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    @Transactional
    public void tearDown() {
        Set<String> tables = entityManager.getMetamodel().getEntities().stream()
            .filter(entity -> entity.getJavaType().getAnnotation(Entity.class) != null)
            .map(entity -> {
                Table tableAnnotation = entity.getJavaType().getAnnotation(Table.class);
                return tableAnnotation != null ? tableAnnotation.name() : null;
            })
            .filter(tableName -> tableName != null && !tableName.isEmpty())
            .collect(Collectors.toSet());

        if (tables.isEmpty()) {
            return;
        }

        try {
            String tableList = String.join(", ", tables);
            jdbcTemplate.execute("TRUNCATE TABLE " + tableList + " RESTART IDENTITY CASCADE");
        } catch (Exception e) {
            throw new RuntimeException("Failed to clean up test data: " + e.getMessage(), e);
        }
    }
}
