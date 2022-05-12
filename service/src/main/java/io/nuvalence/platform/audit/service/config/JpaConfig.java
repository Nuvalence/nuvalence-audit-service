package io.nuvalence.platform.audit.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configures JPA.
 */
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "io.nuvalence.platform.audit.service.repository")
@Configuration
public class JpaConfig {}
