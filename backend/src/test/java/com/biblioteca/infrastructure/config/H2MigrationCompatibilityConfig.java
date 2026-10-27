package com.biblioteca.infrastructure.config;

import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Compatibilidad de sintaxis del seed PostgreSQL con H2, sin cambiar los checksums de produccion. */
@Configuration
@Profile("test")
public class H2MigrationCompatibilityConfig {
    @Bean
    FlywayConfigurationCustomizer migracionesH2() throws IOException {
        var recursos = new PathMatchingResourcePatternResolver().getResources("classpath:db/migration/*.sql");
        List<LoadableResource> migraciones = new ArrayList<>();
        for (var recurso : recursos) {
            String nombre = Objects.requireNonNull(recurso.getFilename());
            String sql;
            try (var entrada = recurso.getInputStream()) { sql = new String(entrada.readAllBytes(), StandardCharsets.UTF_8); }
            String compatible = sql.replace("ON CONFLICT (id) DO NOTHING", "ON CONFLICT DO NOTHING");
            migraciones.add(new LoadableResource() {
                public Reader read() { return new StringReader(compatible); }
                public String getFilename() { return nombre; }
                public String getAbsolutePath() { return "db/migration/" + nombre; }
                public String getAbsolutePathOnDisk() { return getAbsolutePath(); }
                public String getRelativePath() { return nombre; }
            });
        }
        return config -> config.resourceProvider(new ResourceProvider() {
            public LoadableResource getResource(String name) {
                return migraciones.stream().filter(r -> r.getFilename().equals(name)).findFirst().orElse(null);
            }
            public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
                return migraciones.stream().filter(r -> r.getFilename().startsWith(prefix)
                    && Arrays.stream(suffixes).anyMatch(s -> r.getFilename().endsWith(s))).toList();
            }
        });
    }
}
