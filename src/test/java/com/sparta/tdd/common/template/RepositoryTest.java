package com.sparta.tdd.common.template;

import com.sparta.tdd.common.config.TestContainerConfig;
import com.sparta.tdd.common.helper.CleanUp;
import com.sparta.tdd.global.config.AuditConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestContainerConfig.class, CleanUp.class, AuditConfig.class})
public abstract class RepositoryTest {

    @Autowired
    protected CleanUp cleanUp;

    @Autowired
    protected EntityManager em;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }
}