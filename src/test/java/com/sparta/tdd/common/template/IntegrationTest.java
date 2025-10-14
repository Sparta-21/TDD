package com.sparta.tdd.common.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.tdd.common.config.TestContainerConfig;
import com.sparta.tdd.common.helper.CleanUp;
import com.sparta.tdd.global.config.QueryDSLConfig;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import({TestContainerConfig.class, QueryDSLConfig.class})
public abstract class IntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected CleanUp cleanUp;

    @AfterEach
    protected void tearDown() {
        cleanUp.tearDown();
    }
}
