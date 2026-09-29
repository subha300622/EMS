package com.example.ems.asset.service;

import com.example.ems.asset.entity.AssetStatus;
import com.example.ems.asset.entity.AssetTransition;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class AssetStateMachineService {

    private static final Map<AssetTransition, Set<AssetStatus>> ALLOWED_SOURCE_STATES = new EnumMap<>(AssetTransition.class);
    private static final Map<AssetTransition, AssetStatus> TARGET_STATES = new EnumMap<>(AssetTransition.class);

    static {
        ALLOWED_SOURCE_STATES.put(AssetTransition.ACTIVATE, EnumSet.of(AssetStatus.DRAFT));
        TARGET_STATES.put(AssetTransition.ACTIVATE, AssetStatus.ACTIVE);

        ALLOWED_SOURCE_STATES.put(AssetTransition.MAKE_AVAILABLE, EnumSet.of(
                AssetStatus.ACTIVE, AssetStatus.RETURNED, AssetStatus.REPAIR, AssetStatus.IN_MAINTENANCE
        ));
        TARGET_STATES.put(AssetTransition.MAKE_AVAILABLE, AssetStatus.AVAILABLE);

        ALLOWED_SOURCE_STATES.put(AssetTransition.ASSIGN, EnumSet.of(AssetStatus.AVAILABLE));
        TARGET_STATES.put(AssetTransition.ASSIGN, AssetStatus.ASSIGNED);

        ALLOWED_SOURCE_STATES.put(AssetTransition.TRANSFER, EnumSet.of(AssetStatus.ASSIGNED));
        TARGET_STATES.put(AssetTransition.TRANSFER, AssetStatus.ASSIGNED);

        ALLOWED_SOURCE_STATES.put(AssetTransition.RETURN, EnumSet.of(AssetStatus.ASSIGNED));
        TARGET_STATES.put(AssetTransition.RETURN, AssetStatus.RETURNED);

        ALLOWED_SOURCE_STATES.put(AssetTransition.MARK_LOST, EnumSet.of(AssetStatus.ASSIGNED));
        TARGET_STATES.put(AssetTransition.MARK_LOST, AssetStatus.LOST);

        ALLOWED_SOURCE_STATES.put(AssetTransition.MARK_DAMAGED, EnumSet.of(AssetStatus.ASSIGNED));
        TARGET_STATES.put(AssetTransition.MARK_DAMAGED, AssetStatus.DAMAGED);

        ALLOWED_SOURCE_STATES.put(AssetTransition.START_REPAIR, EnumSet.of(AssetStatus.LOST, AssetStatus.DAMAGED));
        TARGET_STATES.put(AssetTransition.START_REPAIR, AssetStatus.REPAIR);

        ALLOWED_SOURCE_STATES.put(AssetTransition.START_MAINTENANCE, EnumSet.of(AssetStatus.AVAILABLE, AssetStatus.DAMAGED, AssetStatus.REPAIR));
        TARGET_STATES.put(AssetTransition.START_MAINTENANCE, AssetStatus.IN_MAINTENANCE);

        ALLOWED_SOURCE_STATES.put(AssetTransition.RESTORE, EnumSet.of(AssetStatus.LOST, AssetStatus.DAMAGED, AssetStatus.REPAIR, AssetStatus.IN_MAINTENANCE));
        TARGET_STATES.put(AssetTransition.RESTORE, AssetStatus.AVAILABLE);

        ALLOWED_SOURCE_STATES.put(AssetTransition.RETIRE, EnumSet.of(AssetStatus.AVAILABLE, AssetStatus.REPAIR, AssetStatus.IN_MAINTENANCE));
        TARGET_STATES.put(AssetTransition.RETIRE, AssetStatus.RETIRED);

        ALLOWED_SOURCE_STATES.put(AssetTransition.DISPOSE, EnumSet.of(AssetStatus.RETIRED));
        TARGET_STATES.put(AssetTransition.DISPOSE, AssetStatus.DISPOSED);
    }

    public AssetStatus getNextStatus(AssetStatus currentStatus, AssetTransition transition) {
        if (currentStatus == null || transition == null) {
            throw new IllegalArgumentException("Current status and transition must not be null");
        }
        Set<AssetStatus> validSources = ALLOWED_SOURCE_STATES.get(transition);
        if (validSources == null || !validSources.contains(currentStatus)) {
            throw new IllegalStateException("Invalid asset transition '" + transition + "' from current status '" + currentStatus + "'");
        }
        return TARGET_STATES.get(transition);
    }

    public boolean canTransition(AssetStatus currentStatus, AssetTransition transition) {
        if (currentStatus == null || transition == null) {
            return false;
        }
        Set<AssetStatus> validSources = ALLOWED_SOURCE_STATES.get(transition);
        return validSources != null && validSources.contains(currentStatus);
    }
}
