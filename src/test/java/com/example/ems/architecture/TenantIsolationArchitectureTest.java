package com.example.ems.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.ems", importOptions = { ImportOption.DoNotIncludeTests.class })
public class TenantIsolationArchitectureTest {

        @ArchTest
        public static final ArchRule ARCH_012_controllers_should_not_access_tenant_context_directly = noClasses().that()
                        .resideInAPackage("..controller..")
                        .should().dependOnClassesThat().haveSimpleName("TenantContext");
}
