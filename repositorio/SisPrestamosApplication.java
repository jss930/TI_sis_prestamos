package pe.edu.unsa.sisprestamos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = "pe.edu.unsa.sisprestamos.repositorio.jpa.entidad")
@EnableJpaRepositories(basePackages = "pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio")
@ComponentScan(basePackages = {
    "pe.edu.unsa.sisprestamos.repositorio",
    "pe.edu.unsa.sisprestamos.dominio",
    "pe.edu.unsa.sisprestamos.aplicacion",
    "pe.edu.unsa.sisprestamos.presentacion"
})
public class SisPrestamosApplication {

    public static void main(String[] args) {
        SpringApplication.run(SisPrestamosApplication.class, args);
    }
}