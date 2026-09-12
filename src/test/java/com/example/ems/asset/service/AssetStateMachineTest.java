package com.example.ems.asset.service;

import com.example.ems.asset.entity.AssetStatus;
import com.example.ems.asset.entity.AssetTransition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AssetStateMachineTest {

    private AssetStateMachineService stateMachineService;

    @BeforeEach
    public void setUp() {
        stateMachineService = new AssetStateMachineService();
    }

    @Test
    public void testActivateTransition() {
        assertTrue(stateMachineService.canTransition(AssetStatus.DRAFT, AssetTransition.ACTIVATE));
        assertEquals(AssetStatus.ACTIVE, stateMachineService.getNextStatus(AssetStatus.DRAFT, AssetTransition.ACTIVATE));

        assertFalse(stateMachineService.canTransition(AssetStatus.ASSIGNED, AssetTransition.ACTIVATE));
        assertThrows(IllegalStateException.class, () -> stateMachineService.getNextStatus(AssetStatus.ASSIGNED, AssetTransition.ACTIVATE));
    }

    @Test
    public void testMakeAvailableTransition() {
        assertTrue(stateMachineService.canTransition(AssetStatus.ACTIVE, AssetTransition.MAKE_AVAILABLE));
        assertTrue(stateMachineService.canTransition(AssetStatus.RETURNED, AssetTransition.MAKE_AVAILABLE));
        assertTrue(stateMachineService.canTransition(AssetStatus.REPAIR, AssetTransition.MAKE_AVAILABLE));
        assertTrue(stateMachineService.canTransition(AssetStatus.IN_MAINTENANCE, AssetTransition.MAKE_AVAILABLE));

        assertEquals(AssetStatus.AVAILABLE, stateMachineService.getNextStatus(AssetStatus.RETURNED, AssetTransition.MAKE_AVAILABLE));
    }

    @Test
    public void testAssignTransition() {
        assertTrue(stateMachineService.canTransition(AssetStatus.AVAILABLE, AssetTransition.ASSIGN));
        assertEquals(AssetStatus.ASSIGNED, stateMachineService.getNextStatus(AssetStatus.AVAILABLE, AssetTransition.ASSIGN));

        assertFalse(stateMachineService.canTransition(AssetStatus.DRAFT, AssetTransition.ASSIGN));
    }

    @Test
    public void testReturnTransition() {
        assertTrue(stateMachineService.canTransition(AssetStatus.ASSIGNED, AssetTransition.RETURN));
        assertEquals(AssetStatus.RETURNED, stateMachineService.getNextStatus(AssetStatus.ASSIGNED, AssetTransition.RETURN));
    }

    @Test
    public void testLostAndDamagedTransitions() {
        assertTrue(stateMachineService.canTransition(AssetStatus.ASSIGNED, AssetTransition.MARK_LOST));
        assertEquals(AssetStatus.LOST, stateMachineService.getNextStatus(AssetStatus.ASSIGNED, AssetTransition.MARK_LOST));

        assertTrue(stateMachineService.canTransition(AssetStatus.ASSIGNED, AssetTransition.MARK_DAMAGED));
        assertEquals(AssetStatus.DAMAGED, stateMachineService.getNextStatus(AssetStatus.ASSIGNED, AssetTransition.MARK_DAMAGED));
    }

    @Test
    public void testRepairAndMaintenanceTransitions() {
        assertTrue(stateMachineService.canTransition(AssetStatus.DAMAGED, AssetTransition.START_REPAIR));
        assertEquals(AssetStatus.REPAIR, stateMachineService.getNextStatus(AssetStatus.DAMAGED, AssetTransition.START_REPAIR));

        assertTrue(stateMachineService.canTransition(AssetStatus.AVAILABLE, AssetTransition.START_MAINTENANCE));
        assertEquals(AssetStatus.IN_MAINTENANCE, stateMachineService.getNextStatus(AssetStatus.AVAILABLE, AssetTransition.START_MAINTENANCE));
    }

    @Test
    public void testRetireAndDisposeTransitions() {
        assertTrue(stateMachineService.canTransition(AssetStatus.AVAILABLE, AssetTransition.RETIRE));
        assertEquals(AssetStatus.RETIRED, stateMachineService.getNextStatus(AssetStatus.AVAILABLE, AssetTransition.RETIRE));

        assertTrue(stateMachineService.canTransition(AssetStatus.RETIRED, AssetTransition.DISPOSE));
        assertEquals(AssetStatus.DISPOSED, stateMachineService.getNextStatus(AssetStatus.RETIRED, AssetTransition.DISPOSE));

        assertFalse(stateMachineService.canTransition(AssetStatus.ASSIGNED, AssetTransition.DISPOSE));
    }
}
