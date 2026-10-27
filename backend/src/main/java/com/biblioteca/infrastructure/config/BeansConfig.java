package com.biblioteca.infrastructure.config;

import com.biblioteca.circulation.domain.policy.PoliticaPrestamo;
import com.biblioteca.circulation.domain.policy.PoliticaPrestamoEstandar;
import com.biblioteca.reservations.domain.policy.PoliticaReserva;
import com.biblioteca.reservations.domain.policy.PoliticaReservaEstandar;
import com.biblioteca.penalties.domain.policy.PoliticaSancion;
import com.biblioteca.penalties.domain.policy.PoliticaSancionEstandar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Las politicas y el reloj viven en el dominio, sin anotaciones de Spring.
 * Se exponen como beans desde infraestructura para mantener el dominio puro.
 */
@Configuration
public class BeansConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Lima"));
    }

    @Bean
    public ZoneId zonaHorariaBiblioteca() {
        return ZoneId.of("America/Lima");
    }

    @Bean
    public PoliticaPrestamo politicaPrestamo() {
        return new PoliticaPrestamoEstandar();
    }

    @Bean
    public PoliticaSancion politicaSancion() {
        return new PoliticaSancionEstandar();
    }

    @Bean
    public PoliticaReserva politicaReserva() {
        return new PoliticaReservaEstandar();
    }
}
