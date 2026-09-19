package com.example.ems.flyway;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.output.ValidateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Flyway Database Schema & Migration Integration Tests")
public class FlywayMigrationIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Verify Flyway Bean is initialized and injected")
    void testFlywayBeanInitialized() {
        assertThat(flyway).isNotNull();
    }

    @Test
    @DisplayName("Verify Flyway validation succeeds without checksum or version mismatch")
    void testFlywayValidationSucceeds() {
        ValidateResult validateResult = flyway.validateWithResult();
        assertThat(validateResult.validationSuccessful)
                .as("Flyway validation errors: %s", validateResult.invalidMigrations)
                .isTrue();
    }

    @Test
    @DisplayName("Verify all migration scripts are applied successfully")
    void testAllMigrationsAppliedSuccessfully() {
        MigrationInfoService infoService = flyway.info();
        MigrationInfo[] allMigrations = infoService.all();

        assertThat(allMigrations).isNotEmpty();
        System.out.println("====== FLYWAY MIGRATION STATUS REPORT ======");
        System.out.println(String.format("Total Migrations Discovered: %d", allMigrations.length));

        int appliedCount = 0;
        for (MigrationInfo info : allMigrations) {
            System.out.println(String.format("Version: %-6s | Description: %-45s | State: %-10s | Type: %s",
                    info.getVersion() != null ? info.getVersion().getVersion() : "N/A",
                    info.getDescription(),
                    info.getState(),
                    info.getType()));

            // Ensure no migrations are in a failed state
            assertThat(info.getState())
                    .as("Migration %s is in failed state!", info.getScript())
                    .isNotEqualTo(MigrationState.FAILED);

            if (info.getState() == MigrationState.SUCCESS || info.getState() == MigrationState.OUT_OF_ORDER) {
                appliedCount++;
            }
        }

        System.out.println(String.format("Total Applied Migrations: %d", appliedCount));
        System.out.println("============================================");

        MigrationInfo current = infoService.current();
        assertThat(current).isNotNull();
        System.out.println("Current Schema Version: " + current.getVersion().getVersion());
    }

    @Test
    @DisplayName("Verify critical core database tables exist in PostgreSQL schema")
    void testCoreDatabaseTablesExist() {
        List<String> expectedTables = Arrays.asList(
                "organizations",
                "users",
                "roles",
                "permissions",
                "employees",
                "attendance",
                "attendance_policies",
                "attendance_adjustments",
                "attendance_breaks",
                "holidays",
                "leaves",
                "leave_policies",
                "leave_types",
                "leave_balances",
                "payroll_runs",
                "salary_structures",
                "goals",
                "assets",
                "support_tickets",
                "flyway_schema_history"
        );

        for (String tableName : expectedTables) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
                    Integer.class,
                    tableName
            );
            assertThat(count)
                    .as("Table '%s' must exist in PostgreSQL public schema", tableName)
                    .isEqualTo(1);
        }
    }
}
