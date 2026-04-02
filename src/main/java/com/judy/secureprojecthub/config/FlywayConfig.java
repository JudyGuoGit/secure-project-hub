package com.judy.secureprojecthub.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

/**
 * Flyway configuration to ensure migrations run before JPA initialization
 */
@Configuration
public class FlywayConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    @Bean(initMethod = "migrate")
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = false)
    public Flyway flyway(DataSource dataSource) {
        logger.info("🔵 Initializing Flyway bean - running migrations...");
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .outOfOrder(false)
                .table("flyway_schema_history")
                .load();
    }
}

