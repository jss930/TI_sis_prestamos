package com.universidad.biblioteca.infraestructura.config;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class JpaConfiguracion {

    @Value("${spring.jpa.properties.hibernate.dialect}")
    private String dialect;

    @Value("${spring.jpa.properties.hibernate.format_sql}")
    private boolean formatSql;

    @Value("${spring.jpa.properties.hibernate.show_sql}")
    private boolean showSql;

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabasePlatform(dialect);
        adapter.setShowSql(showSql);
        adapter.setGenerateDdl(false); // Usamos Flyway para DDL
        return adapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setJpaVendorAdapter(jpaVendorAdapter);
        em.setPackagesToScan("pe.edu.unsa.sisprestamos.repositorio.jpa.entidad");

        Properties properties = new Properties();
        properties.setProperty(AvailableSettings.DIALECT, dialect);
        properties.setProperty(AvailableSettings.FORMAT_SQL, String.valueOf(formatSql));
        properties.setProperty(AvailableSettings.SHOW_SQL, String.valueOf(showSql));
        properties.setProperty(AvailableSettings.HBM2DDL_AUTO, "validate");
        
        // Configuración para JSONB de PostgreSQL
        properties.setProperty("hibernate.types.print.banner", "false");
        
        em.setJpaProperties(properties);
        return em;
    }
}