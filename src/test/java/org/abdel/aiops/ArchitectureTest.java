package org.abdel.aiops;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureTest {

    private static final JavaClasses PROJECT_CLASSES =
            new ClassFileImporter()
                    .withImportOption(new ImportOption.DoNotIncludeTests())
                    .importPackages("org.abdel.aiops");

    @Test
    void domainShouldNotDependOnSpring() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework..")
                .because("Domain must be independent of Spring framework")
                .check(PROJECT_CLASSES);
    }

    @Test
    void domainShouldNotDependOnOuterLayers() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..application..",
                        "..infrastructure..",
                        "..api.."
                )
                .because("Domain must be independent of outer layers")
                .check(PROJECT_CLASSES);
    }

    @Test
    void applicationShouldNotDependOnApiOrInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..api..",
                        "..infrastructure.."
                )
                .because("Application layer must be independent of API and Infrastructure layers")
                .check(PROJECT_CLASSES);
    }

    @Test
    void applicationShouldNotDependOnSpring() {
        noClasses()
                .that()
                .resideInAPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework..")
                .because("Use cases in the application layer should not depend on Spring framework")
                .check(PROJECT_CLASSES);
    }
}
