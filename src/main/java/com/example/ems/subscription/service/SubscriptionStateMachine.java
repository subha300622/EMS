package com.example.ems.subscription.service;

import com.example.ems.organization.entity.SubscriptionStatus;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

public class SubscriptionStateMachine {
    private static final Map<SubscriptionStatus, Set<SubscriptionStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<SubscriptionStatus, Set<SubscriptionStatus>> map = new HashMap<>();
        
        map.put(SubscriptionStatus.INCOMPLETE, Set.of(SubscriptionStatus.PENDING_PAYMENT, SubscriptionStatus.CANCELLED));
        map.put(SubscriptionStatus.PENDING_PAYMENT, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED));
        map.put(SubscriptionStatus.PENDING, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED)); // For backward compatibility
        map.put(SubscriptionStatus.ACTIVE, Set.of(SubscriptionStatus.PAST_DUE, SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED, SubscriptionStatus.SUSPENDED));
        map.put(SubscriptionStatus.PAST_DUE, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED));
        map.put(SubscriptionStatus.SUSPENDED, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED));
        map.put(SubscriptionStatus.TRIAL, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED));
        
        // Terminal states
        map.put(SubscriptionStatus.CANCELLED, Set.of(SubscriptionStatus.ARCHIVED));
        map.put(SubscriptionStatus.EXPIRED, Set.of(SubscriptionStatus.ARCHIVED));
        map.put(SubscriptionStatus.ARCHIVED, Collections.emptySet());

        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public static boolean canTransition(SubscriptionStatus from, SubscriptionStatus to) {
        if (from == to) {
            return true;
        }
        if (from == null) {
            return true; // initial assignment
        }
        Set<SubscriptionStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
