package com.github.miyohide.mymcp.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for McpConfig.
 * Validates requirements 3.1-3.6.
 */
class McpConfigTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // --- Requirement 3.1, 3.2, 3.3: property loading ---

    @SpringBootTest
    @TestPropertySource(properties = {
            "mcp.server.url=http://example.com",
            "mcp.server.connection-timeout=3000",
            "mcp.server.read-timeout=15000"
    })
    static class WhenAllPropertiesAreSet {

        @Autowired
        private McpConfig mcpConfig;

        @Test
        void loadsUrlFromProperties() {
            assertThat(mcpConfig.getUrl()).isEqualTo("http://example.com");
        }

        @Test
        void loadsConnectionTimeoutFromProperties() {
            assertThat(mcpConfig.getConnectionTimeout()).isEqualTo(3000);
        }

        @Test
        void loadsReadTimeoutFromProperties() {
            assertThat(mcpConfig.getReadTimeout()).isEqualTo(15000);
        }
    }

    // --- Requirement 3.4: URL missing causes validation failure ---

    @Test
    void whenUrlIsNull_thenValidationFails() {
        McpConfig config = new McpConfig();
        config.setUrl(null);

        Set<ConstraintViolation<McpConfig>> violations = validator.validate(config);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("url"));
    }

    @Test
    void whenUrlIsBlank_thenValidationFails() {
        McpConfig config = new McpConfig();
        config.setUrl("   ");

        Set<ConstraintViolation<McpConfig>> violations = validator.validate(config);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("url"));
    }

    // --- Requirement 3.5, 3.6: default timeout values ---

    @SpringBootTest
    @TestPropertySource(properties = {
            "mcp.server.url=http://example.com"
    })
    static class WhenTimeoutsAreNotSet {

        @Autowired
        private McpConfig mcpConfig;

        @Test
        void connectionTimeoutDefaultsTo5000() {
            assertThat(mcpConfig.getConnectionTimeout()).isEqualTo(5000);
        }

        @Test
        void readTimeoutDefaultsTo30000() {
            assertThat(mcpConfig.getReadTimeout()).isEqualTo(30000);
        }
    }
}
