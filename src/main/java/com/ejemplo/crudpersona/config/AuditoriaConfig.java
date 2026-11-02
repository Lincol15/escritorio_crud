package com.ejemplo.crudpersona.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditor")
public class AuditoriaConfig {

    /** En una aplicacion de escritorio el autor es el usuario de Windows. */
    @Bean
    public AuditorAware<String> auditor() {
        return () -> Optional.ofNullable(System.getProperty("user.name"));
    }
}
