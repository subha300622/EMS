package com.example.ems.support.service;

import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.repository.MySupportCategoryRepository;
import com.example.ems.support.repository.MySupportTicketRepository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlatformSupportCategoryService {

    @Autowired
    private MySupportCategoryRepository categoryRepository;

    @Autowired
    private MySupportTicketRepository ticketRepository;

    public PlatformCategoryStatsResponse getDashboardStats() {
        List<MySupportCategory> allCategories = categoryRepository.findAll();
        long total = allCategories.size();
        long active = allCategories.stream().filter(c -> c.getStatus() == CategoryStatus.ACTIVE).count();
        long inactive = total - active;

        long totalTickets = ticketRepository.count();
        MySupportCategory mostUsed = null;
        long maxCount = 0;

        for (MySupportCategory cat : allCategories) {
            long count = ticketRepository.countByCategoryIdAndIsDeletedFalse(cat.getId());
            if (count > maxCount) {
                maxCount = count;
                mostUsed = cat;
            }
        }

        PlatformCategoryStatsResponse.MostUsedCategory mostUsedDto = null;
        if (mostUsed != null) {
            double pct = totalTickets == 0 ? 0 : (double) maxCount * 100 / totalTickets;
            mostUsedDto = new PlatformCategoryStatsResponse.MostUsedCategory(
                    mostUsed.getId(), mostUsed.getName(), maxCount, Math.round(pct * 100.0) / 100.0
            );
        }

        return new PlatformCategoryStatsResponse(total, active, inactive, mostUsedDto);
    }

    public Page<MySupportCategory> getCategories(
            String search, String status, String createdBy,
            LocalDateTime createdFrom, LocalDateTime createdTo,
            String sortBy, String order, Pageable pageable) {

        Specification<MySupportCategory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), CategoryStatus.valueOf(status.toUpperCase())));
            }

            if (createdBy != null && !createdBy.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("createdBy")), createdBy.toLowerCase()));
            }

            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }

            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Custom sort parsing
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction dir = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(dir, sortBy);
        } else {
            sort = Sort.by(Sort.Direction.ASC, "displayOrder");
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return categoryRepository.findAll(spec, sortedPageable);
    }

    public MySupportCategory getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
    }

    public MySupportCategory createCategory(PlatformCategoryRequest req, String adminEmail) {
        validateUniqueness(req.getName(), null);

        MySupportCategory cat = new MySupportCategory();
        cat.setName(req.getName());
        cat.setDescription(req.getDescription());
        cat.setIcon(req.getIcon());
        cat.setColor(req.getColor());
        cat.setCreatedBy(adminEmail);
        cat.setUpdatedBy(adminEmail);
        cat.setUpdatedAt(LocalDateTime.now());

        if (req.getStatus() != null) {
            cat.setStatus(CategoryStatus.valueOf(req.getStatus().toUpperCase()));
        }

        // Set display order to next sequence
        List<MySupportCategory> all = categoryRepository.findAll();
        int maxOrder = all.stream().mapToInt(c -> c.getDisplayOrder() == null ? 0 : c.getDisplayOrder()).max().orElse(0);
        cat.setDisplayOrder(maxOrder + 1);

        return categoryRepository.save(cat);
    }

    public MySupportCategory updateCategory(Long id, PlatformCategoryRequest req, String adminEmail) {
        MySupportCategory cat = getCategory(id);
        validateUniqueness(req.getName(), id);

        cat.setName(req.getName());
        cat.setDescription(req.getDescription());
        cat.setIcon(req.getIcon());
        cat.setColor(req.getColor());
        cat.setUpdatedBy(adminEmail);
        cat.setUpdatedAt(LocalDateTime.now());

        if (req.getStatus() != null) {
            cat.setStatus(CategoryStatus.valueOf(req.getStatus().toUpperCase()));
        }

        return categoryRepository.save(cat);
    }

    public MySupportCategory changeStatus(Long id, String status, String adminEmail) {
        MySupportCategory cat = getCategory(id);
        cat.setStatus(CategoryStatus.valueOf(status.toUpperCase()));
        cat.setUpdatedBy(adminEmail);
        cat.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(cat);
    }

    public void deleteCategory(Long id) {
        MySupportCategory cat = getCategory(id);
        if (cat.getIsSystem() != null && cat.getIsSystem()) {
            throw new IllegalArgumentException("System protected categories cannot be deleted.");
        }

        long ticketCount = ticketRepository.countByCategoryIdAndIsDeletedFalse(id);
        if (ticketCount > 0) {
            throw new IllegalArgumentException("Category is assigned to " + ticketCount + " support tickets.");
        }

        cat.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(cat);
    }

    public List<MySupportCategory> reorderCategories(PlatformCategoryReorderRequest req) {
        List<PlatformCategoryReorderRequest.CategoryOrderDto> items = req.getCategories();
        Set<Long> ids = new HashSet<>();
        Set<Integer> orders = new HashSet<>();

        for (PlatformCategoryReorderRequest.CategoryOrderDto dto : items) {
            if (!ids.add(dto.getId())) {
                throw new IllegalArgumentException("Duplicate Category ID in reorder list: " + dto.getId());
            }
            if (!orders.add(dto.getDisplayOrder())) {
                throw new IllegalArgumentException("Duplicate Display Order in reorder list: " + dto.getDisplayOrder());
            }
        }

        for (PlatformCategoryReorderRequest.CategoryOrderDto dto : items) {
            MySupportCategory cat = getCategory(dto.getId());
            cat.setDisplayOrder(dto.getDisplayOrder());
            categoryRepository.save(cat);
        }

        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(MySupportCategory::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    public List<PlatformCategoryAnalyticsResponse> getAnalytics() {
        List<MySupportCategory> all = categoryRepository.findAll();
        long totalTickets = ticketRepository.count();

        List<PlatformCategoryAnalyticsResponse> list = new ArrayList<>();
        for (MySupportCategory cat : all) {
            long count = ticketRepository.countByCategoryIdAndIsDeletedFalse(cat.getId());
            double pct = totalTickets == 0 ? 0 : (double) count * 100 / totalTickets;

            long openCount = ticketRepository.countByCategoryIdAndStatusAndIsDeletedFalse(cat.getId(), SupportTicketStatus.OPEN) +
                             ticketRepository.countByCategoryIdAndStatusAndIsDeletedFalse(cat.getId(), SupportTicketStatus.IN_PROGRESS) +
                             ticketRepository.countByCategoryIdAndStatusAndIsDeletedFalse(cat.getId(), SupportTicketStatus.ASSIGNED);

            long closedCount = ticketRepository.countByCategoryIdAndStatusAndIsDeletedFalse(cat.getId(), SupportTicketStatus.CLOSED) +
                               ticketRepository.countByCategoryIdAndStatusAndIsDeletedFalse(cat.getId(), SupportTicketStatus.RESOLVED);

            List<MySupportTicket> resolvedTickets = ticketRepository.findByCategoryIdAndIsDeletedFalse(cat.getId()).stream()
                    .filter(t -> t.getResolvedAt() != null)
                    .collect(Collectors.toList());

            String avgRes = "N/A";
            if (!resolvedTickets.isEmpty()) {
                long totalMins = 0;
                for (MySupportTicket t : resolvedTickets) {
                    totalMins += Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes();
                }
                long avgMins = totalMins / resolvedTickets.size();
                if (avgMins < 60) {
                    avgRes = avgMins + " mins";
                } else {
                    avgRes = String.format("%.1f hrs", (double) avgMins / 60);
                }
            }

            list.add(new PlatformCategoryAnalyticsResponse(
                    cat.getName(), count, Math.round(pct * 100.0) / 100.0, avgRes, openCount, closedCount
            ));
        }

        return list;
    }

    public List<PlatformCategoryOption> getOptions() {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getStatus() == CategoryStatus.ACTIVE)
                .sorted(Comparator.comparing(MySupportCategory::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(c -> new PlatformCategoryOption(c.getId(), c.getName(), c.getColor(), c.getIcon()))
                .collect(Collectors.toList());
    }

    private void validateUniqueness(String name, Long currentId) {
        Optional<MySupportCategory> existing = categoryRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            if (currentId == null || !existing.get().getId().equals(currentId)) {
                throw new IllegalArgumentException("A category with the name '" + name + "' already exists.");
            }
        }
    }
}
