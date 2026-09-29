package com.example.ems.subscription.service;

import com.example.ems.organization.entity.InvoiceStatus;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

public class InvoiceStateMachine {
    private static final Map<InvoiceStatus, Set<InvoiceStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<InvoiceStatus, Set<InvoiceStatus>> map = new HashMap<>();
        
        map.put(InvoiceStatus.DRAFT, Set.of(InvoiceStatus.ISSUED, InvoiceStatus.VOID));
        map.put(InvoiceStatus.ISSUED, Set.of(InvoiceStatus.PAID, InvoiceStatus.VOID, InvoiceStatus.OVERDUE));
        map.put(InvoiceStatus.PAID, Collections.emptySet());
        map.put(InvoiceStatus.VOID, Collections.emptySet());
        map.put(InvoiceStatus.OVERDUE, Set.of(InvoiceStatus.PAID, InvoiceStatus.VOID));
        
        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public static boolean canTransition(InvoiceStatus from, InvoiceStatus to) {
        if (from == to) {
            return true;
        }
        if (from == null) {
            return true;
        }
        Set<InvoiceStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
