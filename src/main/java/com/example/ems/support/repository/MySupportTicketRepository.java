package com.example.ems.support.repository;

import com.example.ems.employee.entity.Employee;
import com.example.ems.support.entity.MySupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MySupportTicketRepository extends JpaRepository<MySupportTicket, Long>, JpaSpecificationExecutor<MySupportTicket> {
    List<MySupportTicket> findByEmployee(Employee employee);
    List<MySupportTicket> findByEmployeeEmail(String email);
    Optional<MySupportTicket> findByTicketNumber(String ticketNumber);

    @Query("SELECT t FROM MySupportTicket t WHERE t.employee.email = :email " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:search IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MySupportTicket> findByFilters(@Param("email") String email,
                                        @Param("status") com.example.ems.support.entity.SupportTicketStatus status,
                                        @Param("priority") com.example.ems.support.entity.SupportTicketPriority priority,
                                        @Param("categoryId") Long categoryId,
                                        @Param("search") String search,
                                        Pageable pageable);

    long countByCategoryIdAndIsDeletedFalse(Long categoryId);
    long countByCategoryIdAndStatusAndIsDeletedFalse(Long categoryId, com.example.ems.support.entity.SupportTicketStatus status);
    List<MySupportTicket> findByCategoryIdAndIsDeletedFalse(Long categoryId);

    // SLA metrics helper queries
    long countByPriorityAndIsDeletedFalse(com.example.ems.support.entity.SupportTicketPriority priority);
    long countByIsDeletedFalse();

    @Query("SELECT COUNT(t) FROM MySupportTicket t WHERE t.isDeleted = false " +
           "AND t.slaResolutionDueAt IS NOT NULL " +
           "AND ((t.resolvedAt IS NOT NULL AND t.resolvedAt > t.slaResolutionDueAt) " +
           "OR (t.resolvedAt IS NULL AND :now > t.slaResolutionDueAt))")
    long countBreachedTickets(@Param("now") java.time.LocalDateTime now);

    @Query("SELECT COUNT(t) FROM MySupportTicket t WHERE t.isDeleted = false AND t.priority = :priority " +
           "AND t.slaResolutionDueAt IS NOT NULL " +
           "AND ((t.resolvedAt IS NOT NULL AND t.resolvedAt > t.slaResolutionDueAt) " +
           "OR (t.resolvedAt IS NULL AND :now > t.slaResolutionDueAt))")
    long countBreachedTicketsByPriority(@Param("priority") com.example.ems.support.entity.SupportTicketPriority priority,
                                        @Param("now") java.time.LocalDateTime now);
}
