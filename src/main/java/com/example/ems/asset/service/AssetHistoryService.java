package com.example.ems.asset.service;

import com.example.ems.asset.dto.AssetDtos.AssetHistoryResponse;
import com.example.ems.asset.entity.AssetEventType;
import com.example.ems.asset.entity.AssetHistory;
import com.example.ems.asset.entity.AssetLocation;
import com.example.ems.asset.repository.AssetHistoryRepository;
import com.example.ems.asset.repository.AssetLocationRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AssetHistoryService {

    @Autowired
    private AssetHistoryRepository historyRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AssetLocationRepository locationRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void recordHistory(Long organizationId, Long assetId, AssetEventType eventType,
                              String oldStatus, String newStatus,
                              Long fromEmployeeId, Long toEmployeeId,
                              Long fromLocationId, Long toLocationId,
                              String performedBy, String referenceId, String remarks) {

        AssetHistory history = new AssetHistory(
                organizationId, assetId, eventType, oldStatus, newStatus,
                fromEmployeeId, toEmployeeId, fromLocationId, toLocationId,
                performedBy, referenceId, remarks
        );
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<AssetHistoryResponse> getAssetHistory(Long organizationId, Long assetId) {
        List<AssetHistory> list = historyRepository.findByAssetIdAndOrganizationIdOrderByPerformedAtDesc(assetId, organizationId);
        return mapToResponses(list);
    }

    private List<AssetHistoryResponse> mapToResponses(List<AssetHistory> list) {
        Set<Long> empIds = list.stream()
                .flatMap(h -> Stream.of(h.getFromEmployeeId(), h.getToEmployeeId()))
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> locIds = list.stream()
                .flatMap(h -> Stream.of(h.getFromLocationId(), h.getToLocationId()))
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, String> empNames = employeeRepository.findAllById(empIds).stream()
                .collect(Collectors.toMap(Employee::getId, e -> e.getFirstName() + " " + e.getLastName()));

        Map<Long, String> locNames = locationRepository.findAllById(locIds).stream()
                .collect(Collectors.toMap(AssetLocation::getId, AssetLocation::getLocationName));

        return list.stream().map(h -> new AssetHistoryResponse(
                h.getId(),
                h.getAssetId(),
                h.getEventType(),
                h.getOldStatus(),
                h.getNewStatus(),
                h.getFromEmployeeId(),
                h.getFromEmployeeId() != null ? empNames.get(h.getFromEmployeeId()) : null,
                h.getToEmployeeId(),
                h.getToEmployeeId() != null ? empNames.get(h.getToEmployeeId()) : null,
                h.getFromLocationId(),
                h.getFromLocationId() != null ? locNames.get(h.getFromLocationId()) : null,
                h.getToLocationId(),
                h.getToLocationId() != null ? locNames.get(h.getToLocationId()) : null,
                h.getPerformedBy(),
                h.getPerformedAt(),
                h.getReferenceId(),
                h.getRemarks()
        )).collect(Collectors.toList());
    }
}
