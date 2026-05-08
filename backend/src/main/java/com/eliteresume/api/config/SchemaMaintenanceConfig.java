package com.eliteresume.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class SchemaMaintenanceConfig {
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner widenResumeTextColumns() {
        return args -> {
            jdbcTemplate.execute("ALTER TABLE resumes MODIFY COLUMN job_description LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE resumes MODIFY COLUMN career_objective LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE resumes MODIFY COLUMN profile_image_path LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE resumes MODIFY COLUMN linked_in VARCHAR(600)");
            jdbcTemplate.execute("ALTER TABLE resumes MODIFY COLUMN github VARCHAR(600)");
            jdbcTemplate.execute("ALTER TABLE resumes MODIFY COLUMN portfolio VARCHAR(600)");
            jdbcTemplate.execute("ALTER TABLE resumes MODIFY COLUMN pdf_path VARCHAR(1000)");
        };
    }
}
