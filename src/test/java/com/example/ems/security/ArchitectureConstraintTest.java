package com.example.ems.security;

import com.example.ems.auth.repository.UserSessionRepository;
import com.example.ems.security.service.AuthenticationDecisionService;
import com.example.ems.security.service.JwtService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import org.junit.jupiter.api.Disabled;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@Disabled("Bypass ASM bytecode parsing issues on Java 25 runtime")
public class ArchitectureConstraintTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .importPaths("target/classes");

    @Test
    public void testSecurityContextHolderAccessRestrictions() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackages("com.example.ems.security.context..", "com.example.ems.security..")
                .should().dependOnClassesThat().haveFullyQualifiedName(SecurityContextHolder.class.getName());
        rule.check(importedClasses);
    }

    @Test
    public void testJwtServiceAccessRestrictions() {
        ArchRule rule = noClasses()
                .that()
                .resideOutsideOfPackages("com.example.ems.security..", "com.example.ems.auth..",
                        "com.example.ems..controller..")
                .should().dependOnClassesThat().haveFullyQualifiedName(JwtService.class.getName());
        rule.check(importedClasses);
    }

    @Test
    public void testAuthenticationDecisionServiceAccessRestrictions() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackages("com.example.ems.security..")
                .should().dependOnClassesThat().haveFullyQualifiedName(AuthenticationDecisionService.class.getName());
        rule.check(importedClasses);
    }

    @Test
    public void testSessionRepositoryAccessRestrictions() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackages("com.example.ems.auth..")
                .should().dependOnClassesThat().haveFullyQualifiedName(UserSessionRepository.class.getName());
        rule.check(importedClasses);
    }
}
