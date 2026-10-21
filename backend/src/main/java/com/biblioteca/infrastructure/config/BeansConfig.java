package com.biblioteca.infrastructure.config;

import com.biblioteca.circulation.domain.policy.PoliticaPrestamo;
import com.biblioteca.circulation.domain.policy.PoliticaPrestamoEstandar;
import com.biblioteca.penalties.domain.policy.PoliticaSancion;
import com.biblioteca.penalties.domain.policy.PoliticaSancionEstandar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Las politicas y el reloj viven en el dominio, sin anotaciones de Spring.
 * Se exponen como beans desde infraestructura para mantener el dominio puro.
 */
@Configuration
public class BeansConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public PoliticaPrestamo politicaPrestamo() {
        return new PoliticaPrestamoEstandar();
    }

    @Bean
    public PoliticaSancion politicaSancion() {
        return new PoliticaSancionEstandar();
    }
}
