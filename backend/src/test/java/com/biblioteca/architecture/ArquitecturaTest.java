package com.biblioteca.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Reglas de arquitectura ejecutables. Si alguien importa Spring en el dominio
 * o un caso de uso depende de un adaptador, el build falla.
 */
@AnalyzeClasses(packages = "com.biblioteca", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    @ArchTest
    static final ArchRule dominio_puro = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "org.hibernate..",
                    "com.fasterxml.jackson..",
                    "..infrastructure..");

    @ArchTest
    static final ArchRule puertos_de_repositorio_son_interfaces = classes()
            .that().resideInAPackage("..domain.repository..")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule aplicacion_no_depende_de_infraestructura = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
}
