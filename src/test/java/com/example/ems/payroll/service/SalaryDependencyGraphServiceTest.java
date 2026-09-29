package com.example.ems.payroll.service;

import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.validation.SalaryDependencyNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SalaryDependencyGraphServiceTest {

    private SalaryDependencyGraphService graphService;
    private SalaryStructure structure;

    @BeforeEach
    void setUp() {
        graphService = new SalaryDependencyGraphService();
        structure = new SalaryStructure(1L, "Test", "TEST", null, "INR", PayFrequency.MONTHLY, null, null);
    }

    private SalaryComponent createComponent(Long id, String name, String code) {
        SalaryComponent c = new SalaryComponent(1L, name, code, null, SalaryComponentType.EARNING, true, true);
        c.setId(id);
        return c;
    }

    @Test
    @DisplayName("Linear Chain: Basic -> Housing -> Transport has no cycle and produces correct topological order")
    void testLinearDependencyChain() {
        SalaryComponent basic = createComponent(1L, "Basic", "BASIC");
        SalaryComponent housing = createComponent(2L, "Housing", "HOUSING");
        SalaryComponent transport = createComponent(3L, "Transport", "TRANSPORT");

        SalaryStructureComponent ssc1 = new SalaryStructureComponent(structure, basic, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(30000), null, null, 1);
        SalaryStructureComponent ssc2 = new SalaryStructureComponent(structure, housing, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basic, null, BigDecimal.valueOf(25), null, 2);
        SalaryStructureComponent ssc3 = new SalaryStructureComponent(structure, transport, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, housing, null, BigDecimal.valueOf(10), null, 3);

        List<SalaryStructureComponent> components = List.of(ssc1, ssc2, ssc3);

        Map<Long, SalaryDependencyNode> graph = graphService.buildGraph(components);

        Optional<String> cycle = graphService.detectCycle(graph);
        assertFalse(cycle.isPresent());

        List<Long> order = graphService.topologicalSort(graph);
        assertEquals(List.of(1L, 2L, 3L), order);
    }

    @Test
    @DisplayName("Branching Tree: Basic -> (Housing, Food) and Housing -> Transport")
    void testBranchingTree() {
        SalaryComponent basic = createComponent(1L, "Basic", "BASIC");
        SalaryComponent housing = createComponent(2L, "Housing", "HOUSING");
        SalaryComponent food = createComponent(3L, "Food", "FOOD");
        SalaryComponent transport = createComponent(4L, "Transport", "TRANSPORT");

        SalaryStructureComponent ssc1 = new SalaryStructureComponent(structure, basic, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(30000), null, null, 1);
        SalaryStructureComponent ssc2 = new SalaryStructureComponent(structure, housing, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basic, null, BigDecimal.valueOf(25), null, 2);
        SalaryStructureComponent ssc3 = new SalaryStructureComponent(structure, food, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basic, null, BigDecimal.valueOf(10), null, 3);
        SalaryStructureComponent ssc4 = new SalaryStructureComponent(structure, transport, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, housing, null, BigDecimal.valueOf(5), null, 4);

        List<SalaryStructureComponent> components = List.of(ssc1, ssc2, ssc3, ssc4);

        Map<Long, SalaryDependencyNode> graph = graphService.buildGraph(components);

        Optional<String> cycle = graphService.detectCycle(graph);
        assertFalse(cycle.isPresent());

        List<Long> order = graphService.topologicalSort(graph);

        // Basic (1) must be before Housing (2), Food (3), Transport (4)
        assertTrue(order.indexOf(1L) < order.indexOf(2L));
        assertTrue(order.indexOf(1L) < order.indexOf(3L));
        // Housing (2) must be before Transport (4)
        assertTrue(order.indexOf(2L) < order.indexOf(4L));
    }

    @Test
    @DisplayName("Direct Cycle: A -> B and B -> A is detected")
    void testDirectCycleDetection() {
        SalaryComponent compA = createComponent(1L, "Component A", "COMP_A");
        SalaryComponent compB = createComponent(2L, "Component B", "COMP_B");

        SalaryStructureComponent ssc1 = new SalaryStructureComponent(structure, compA, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, compB, null, BigDecimal.valueOf(20), null, 1);
        SalaryStructureComponent ssc2 = new SalaryStructureComponent(structure, compB, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, compA, null, BigDecimal.valueOf(10), null, 2);

        Map<Long, SalaryDependencyNode> graph = graphService.buildGraph(List.of(ssc1, ssc2));

        Optional<String> cycle = graphService.detectCycle(graph);
        assertTrue(cycle.isPresent());
        assertTrue(cycle.get().contains("COMP_A") && cycle.get().contains("COMP_B"));
    }

    @Test
    @DisplayName("Transitive 4-node Cycle: A -> B -> C -> D -> A is detected")
    void testTransitiveCycleDetection() {
        SalaryComponent compA = createComponent(1L, "A", "COMP_A");
        SalaryComponent compB = createComponent(2L, "B", "COMP_B");
        SalaryComponent compC = createComponent(3L, "C", "COMP_C");
        SalaryComponent compD = createComponent(4L, "D", "COMP_D");

        SalaryStructureComponent ssc1 = new SalaryStructureComponent(structure, compA, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, compB, null, BigDecimal.valueOf(10), null, 1);
        SalaryStructureComponent ssc2 = new SalaryStructureComponent(structure, compB, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, compC, null, BigDecimal.valueOf(10), null, 2);
        SalaryStructureComponent ssc3 = new SalaryStructureComponent(structure, compC, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, compD, null, BigDecimal.valueOf(10), null, 3);
        SalaryStructureComponent ssc4 = new SalaryStructureComponent(structure, compD, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, compA, null, BigDecimal.valueOf(10), null, 4);

        Map<Long, SalaryDependencyNode> graph = graphService.buildGraph(List.of(ssc1, ssc2, ssc3, ssc4));

        Optional<String> cycle = graphService.detectCycle(graph);
        assertTrue(cycle.isPresent());
    }
}
