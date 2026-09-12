package com.example.ems.asset.service;

import com.example.ems.asset.dto.AssetDtos.CreateLocationRequest;
import com.example.ems.asset.dto.AssetDtos.LocationResponse;
import com.example.ems.asset.entity.AssetLocation;
import com.example.ems.asset.entity.AssignmentStatus;
import com.example.ems.asset.repository.AssetAssignmentRepository;
import com.example.ems.asset.repository.AssetLocationRepository;
import com.example.ems.asset.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetLocationService {

    @Autowired
    private AssetLocationRepository locationRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetAssignmentRepository assignmentRepository;

    @Transactional
    public LocationResponse createLocation(Long organizationId, CreateLocationRequest request) {
        String code = request.getLocationCode().trim();
        String name = request.getLocationName().trim();

        if (locationRepository.existsByOrganizationIdAndLocationCodeIgnoreCase(organizationId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Location code '" + code + "' already exists in this organization");
        }
        if (locationRepository.existsByOrganizationIdAndLocationNameIgnoreCase(organizationId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Location name '" + name + "' already exists in this organization");
        }

        AssetLocation parent = null;
        if (request.getParentId() != null) {
            parent = locationRepository.findByIdAndOrganizationId(request.getParentId(), organizationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent location not found or belongs to another organization"));
        }

        AssetLocation location = new AssetLocation(organizationId, name, code, parent);
        location = locationRepository.save(location);

        return mapToResponse(location);
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getLocations(Long organizationId, boolean activeOnly) {
        List<AssetLocation> list = activeOnly ?
                locationRepository.findByOrganizationIdAndActiveTrue(organizationId) :
                locationRepository.findByOrganizationId(organizationId);

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LocationResponse getLocationById(Long organizationId, Long locationId) {
        AssetLocation location = locationRepository.findByIdAndOrganizationId(locationId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset Location not found with id " + locationId));
        return mapToResponse(location);
    }

    @Transactional
    public LocationResponse updateLocation(Long organizationId, Long locationId, CreateLocationRequest request) {
        AssetLocation location = locationRepository.findByIdAndOrganizationId(locationId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset Location not found with id " + locationId));

        String code = request.getLocationCode().trim();
        String name = request.getLocationName().trim();

        if (locationRepository.existsByOrganizationIdAndLocationCodeIgnoreCaseAndIdNot(organizationId, code, locationId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Location code '" + code + "' already exists in this organization");
        }
        if (locationRepository.existsByOrganizationIdAndLocationNameIgnoreCaseAndIdNot(organizationId, name, locationId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Location name '" + name + "' already exists in this organization");
        }

        AssetLocation parent = null;
        if (request.getParentId() != null) {
            if (request.getParentId().equals(locationId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location cannot reference itself as parent");
            }
            parent = locationRepository.findByIdAndOrganizationId(request.getParentId(), organizationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent location not found or belongs to another organization"));
        }

        location.setLocationCode(code);
        location.setLocationName(name);
        location.setParent(parent);

        return mapToResponse(locationRepository.save(location));
    }

    @Transactional
    public void deleteLocation(Long organizationId, Long locationId) {
        AssetLocation location = locationRepository.findByIdAndOrganizationId(locationId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset Location not found with id " + locationId));

        if (assetRepository.existsByLocationIdAndDeletedFalse(locationId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "LOCATION_IN_USE: Cannot delete location that currently has assets assigned to it.");
        }
        if (assignmentRepository.existsByLocationIdAndStatus(locationId, AssignmentStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "LOCATION_IN_USE: Cannot delete location that has active assignments.");
        }

        location.setActive(false);
        locationRepository.save(location);
    }

    private LocationResponse mapToResponse(AssetLocation location) {
        return new LocationResponse(
                location.getId(),
                location.getLocationName(),
                location.getLocationCode(),
                location.getParent() != null ? location.getParent().getId() : null,
                location.getParent() != null ? location.getParent().getLocationName() : null,
                location.isActive()
        );
    }
}
