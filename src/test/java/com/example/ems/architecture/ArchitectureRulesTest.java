package com.example.ems.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.ems", importOptions = { ImportOption.DoNotIncludeTests.class })
public class ArchitectureRulesTest {

        @ArchTest
        public static final ArchRule ARCH_004_domain_should_not_depend_on_controllers = noClasses().that()
                        .resideInAPackage("..domain..")
                        .should().dependOnClassesThat().resideInAPackage("..controller..");

        @ArchTest
        public static final ArchRule ARCH_006_domain_should_not_depend_on_spring_web = noClasses().that()
                        .resideInAPackage("..domain..")
                        .should().dependOnClassesThat().resideInAPackage("org.springframework.web..");
}
