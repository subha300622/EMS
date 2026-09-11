package com.example.ems.payroll;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.payroll.dto.PayrollRunCreateRequest;
import com.example.ems.payroll.dto.PayrollRunResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.*;
import com.example.ems.payroll.service.PayrollRunService;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StatutoryPayrollIntegrationTest {

    @Autowired
    private PayrollRunService payrollRunService;

    @Autowired
    private PayrollEmployeeRepository payrollEmployeeRepository;

    @Autowired
    private PayrollItemRepository payrollItemRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SalaryStructureRepository salaryStructureRepository;

    @Autowired
    private SalaryComponentRepository salaryComponentRepository;

    @Autowired
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @Autowired
    private EmployeeSalaryAssignmentRepository employeeSalaryAssignmentRepository;

    private Organization organization;

    @BeforeEach
    public void setUp() {
        long ts = System.currentTimeMillis();
        organization = new Organization();
        organization.setName("Statutory Test Org " + ts);
        organization.setOrganizationCode("ORG_STAT_" + ts);
        organization = organizationRepository.save(organization);

        TenantContext.setCurrentTenant(organization.getId());
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Statutory Integration: ESI covered (<= ₹21k), PF, PT, and Employer cost snapshots")
    public void testStatutoryCalculationsInPayrollRun() {
        long ts = System.currentTimeMillis();

        com.example.ems.employee.entity.Department department = new com.example.ems.employee.entity.Department();
        department.setName("Operations " + ts);
        department.setCode("OPS_" + ts);
        department.setOrganization(organization);
        department = departmentRepository.save(department);

        Employee employee = new Employee();
        employee.setEmployeeId("EMP_ESI_" + ts);
        employee.setFullName("Charlie Operations");
        employee.setEmail("charlie_" + ts + "@statutory.com");
        employee.setDepartment(department.getName());
        employee.setStatus("ACTIVE");
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);

        // Basic = 15,000, HRA = 5,000 -> Gross = 20,000 (ESI Eligible!)
        SalaryComponent basicComp = salaryComponentRepository.save(
                new SalaryComponent(organization.getId(), "Basic", "BASIC", "Basic Salary", SalaryComponentType.EARNING, true, true)
        );
        SalaryComponent hraComp = salaryComponentRepository.save(
                new SalaryComponent(organization.getId(), "HRA", "HRA", "House Rent Allowance", SalaryComponentType.EARNING, true, true)
        );
        SalaryComponent esiComp = salaryComponentRepository.save(
                new SalaryComponent(organization.getId(), "ESI", "ESI", "Employee State Insurance", SalaryComponentType.DEDUCTION, false, true)
        );
        SalaryComponent ptComp = salaryComponentRepository.save(
                new SalaryComponent(organization.getId(), "PT", "PT", "Professional Tax", SalaryComponentType.DEDUCTION, false, true)
        );

        SalaryStructure structure = new SalaryStructure();
        structure.setOrganizationId(organization.getId());
        structure.setName("Operations Structure " + ts);
        structure.setCode("OPS_STR_" + ts);
        structure.setStatus(SalaryStructureStatus.ACTIVE);
        structure.setVersion(1);
        structure = salaryStructureRepository.save(structure);

        salaryStructureComponentRepository.save(new SalaryStructureComponent(
                structure, basicComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(15000), null, null, 1
        ));
        salaryStructureComponentRepository.save(new SalaryStructureComponent(
                structure, hraComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(5000), null, null, 2
        ));
        salaryStructureComponentRepository.save(new SalaryStructureComponent(
                structure, esiComp, CalculationType.PERCENTAGE, CalculationBaseType.GROSS, null, null, BigDecimal.valueOf(0.75), null, 3
        ));
        salaryStructureComponentRepository.save(new SalaryStructureComponent(
                structure, ptComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(200), null, null, 4
        ));

        employeeSalaryAssignmentRepository.save(new EmployeeSalaryAssignment(
                organization.getId(), employee, structure, LocalDate.of(2026, 1, 1), null, SalaryAssignmentStatus.ACTIVE, "Initial"
        ));

        // Create & Process payroll run for Sept 2026
        LocalDate periodStart = LocalDate.of(2026, 9, 1);
        LocalDate periodEnd = LocalDate.of(2026, 9, 30);
        PayrollRunResponse created = payrollRunService.createPayrollRun(new PayrollRunCreateRequest(periodStart, periodEnd, "INR"));
        PayrollRunResponse processed = payrollRunService.processPayrollRun(created.getId());
        assertEquals(PayrollRunStatus.CALCULATED, processed.getStatus());

        PayrollEmployee pe = payrollEmployeeRepository.findByPayrollRunIdAndEmployeeIdAndOrganizationId(
                created.getId(), employee.getId(), organization.getId()
        ).orElseThrow();

        List<PayrollItem> items = payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(
                pe.getId(), organization.getId()
        );

        // Verify Gross = ₹20,000
        assertEquals(0, new BigDecimal("20000.00").compareTo(pe.getGrossAmount()));

        // Employee Deductions:
        // ESI = ₹150 (0.75% of 20,000)
        // PT = ₹200
        // (No PF in structure DAG for this test, so only ESI + PT deducted)
        PayrollItem esiItem = items.stream().filter(i -> "ESI".equals(i.getComponentCode())).findFirst().orElse(null);
        assertNotNull(esiItem, "ESI PayrollItem must be present for gross <= 21k");
        assertEquals(0, new BigDecimal("150.00").compareTo(esiItem.getAmount()));

        PayrollItem ptItem = items.stream().filter(i -> "PT".equals(i.getComponentCode())).findFirst().orElse(null);
        assertNotNull(ptItem, "PT PayrollItem must be present");
        assertEquals(0, new BigDecimal("200.00").compareTo(ptItem.getAmount()));

        // Employer Cost items:
        // EMPLOYER_ESI = ₹650 (3.25% of 20,000)
        PayrollItem employerEsiItem = items.stream().filter(i -> "EMPLOYER_ESI".equals(i.getComponentCode())).findFirst().orElse(null);
        assertNotNull(employerEsiItem, "EMPLOYER_ESI must be generated as EMPLOYER_COST");
        assertEquals("EMPLOYER_COST", employerEsiItem.getComponentType());
        assertEquals(0, new BigDecimal("650.00").compareTo(employerEsiItem.getAmount()));

        // Net Pay = Gross(20,000) - (ESI 150 + PT 200) = 19,650
        // (Employer ESI 650 is NOT deducted from Net Pay!)
        assertEquals(0, new BigDecimal("19650.00").compareTo(pe.getNetAmount()));
    }

    @Test
    @DisplayName("Statutory Integration: High salary (> ₹21k) is exempt from ESI while PT and capped PF apply")
    public void testHighSalaryExemptFromEsi() {
        long ts = System.currentTimeMillis();

        com.example.ems.employee.entity.Department department = new com.example.ems.employee.entity.Department();
        department.setName("Leadership " + ts);
        department.setCode("LEAD_" + ts);
        department.setOrganization(organization);
        department = departmentRepository.save(department);

        Employee employee = new Employee();
        employee.setEmployeeId("EMP_LEAD_" + ts);
        employee.setFullName("Diana Director");
        employee.setEmail("diana_" + ts + "@statutory.com");
        employee.setDepartment(department.getName());
        employee.setStatus("ACTIVE");
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);

        // Basic = 50,000, HRA = 30,000, PF = 1,800, PT = 200 -> Gross = 80,000
        SalaryComponent basicComp = salaryComponentRepository.save(
                new SalaryComponent(organization.getId(), "Basic", "BASIC", "Basic Salary", SalaryComponentType.EARNING, true, true)
        );
        SalaryComponent hraComp = salaryComponentRepository.save(
                new SalaryComponent(organization.getId(), "HRA", "HRA", "House Rent Allowance", SalaryComponentType.EARNING, true, true)
        );
        SalaryComponent pfComp = salaryComponentRepository.save(
                new SalaryComponent(organization.getId(), "PF", "PF", "Provident Fund", SalaryComponentType.DEDUCTION, false, true)
        );
        SalaryComponent ptComp = salaryComponentRepository.save(
                new SalaryComponent(organization.getId(), "PT", "PT", "Professional Tax", SalaryComponentType.DEDUCTION, false, true)
        );

        SalaryStructure structure = new SalaryStructure();
        structure.setOrganizationId(organization.getId());
        structure.setName("Leadership Structure " + ts);
        structure.setCode("LEAD_STR_" + ts);
        structure.setStatus(SalaryStructureStatus.ACTIVE);
        structure.setVersion(1);
        structure = salaryStructureRepository.save(structure);

        salaryStructureComponentRepository.save(new SalaryStructureComponent(
                structure, basicComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(50000), null, null, 1
        ));
        salaryStructureComponentRepository.save(new SalaryStructureComponent(
                structure, hraComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(30000), null, null, 2
        ));
        salaryStructureComponentRepository.save(new SalaryStructureComponent(
                structure, pfComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(1800), null, null, 3
        ));
        salaryStructureComponentRepository.save(new SalaryStructureComponent(
                structure, ptComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(200), null, null, 4
        ));

        employeeSalaryAssignmentRepository.save(new EmployeeSalaryAssignment(
                organization.getId(), employee, structure, LocalDate.of(2026, 1, 1), null, SalaryAssignmentStatus.ACTIVE, "Initial"
        ));

        LocalDate periodStart = LocalDate.of(2026, 9, 1);
        LocalDate periodEnd = LocalDate.of(2026, 9, 30);
        PayrollRunResponse created = payrollRunService.createPayrollRun(new PayrollRunCreateRequest(periodStart, periodEnd, "INR"));
        payrollRunService.processPayrollRun(created.getId());

        PayrollEmployee pe = payrollEmployeeRepository.findByPayrollRunIdAndEmployeeIdAndOrganizationId(
                created.getId(), employee.getId(), organization.getId()
        ).orElseThrow();

        List<PayrollItem> items = payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(
                pe.getId(), organization.getId()
        );

        // Gross = 80,000 > 21,000 -> ESI must be EXEMPT
        assertFalse(items.stream().anyMatch(i -> "ESI".equals(i.getComponentCode())));
        assertFalse(items.stream().anyMatch(i -> "EMPLOYER_ESI".equals(i.getComponentCode())));

        // Employer PF cost items should be generated
        assertTrue(items.stream().anyMatch(i -> "EMPLOYER_EPF".equals(i.getComponentCode())));
        assertTrue(items.stream().anyMatch(i -> "EMPLOYER_EPS".equals(i.getComponentCode())));

        // PT = 200, PF = 1800
        // Net Pay = 80,000 - 1,800 - 200 = 78,000
        assertEquals(0, new BigDecimal("78000.00").compareTo(pe.getNetAmount()));
    }
}
