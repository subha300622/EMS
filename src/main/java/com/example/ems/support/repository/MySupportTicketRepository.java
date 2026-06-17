package com.example.ems.support.repository;

import com.example.ems.employee.entity.Employee;
import com.example.ems.support.entity.MySupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MySupportTicketRepository extends JpaRepository<MySupportTicket, Long> {
    List<MySupportTicket> findByEmployee(Employee employee);
    List<MySupportTicket> findByEmployeeEmail(String email);
    Optional<MySupportTicket> findByTicketNumber(String ticketNumber);

    @Query("SELECT t FROM MySupportTicket t WHERE t.employee.email = :email " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:search IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MySupportTicket> findByFilters(@Param("email") String email,
                                        @Param("status") String status,
                                        @Param("priority") String priority,
                                        @Param("categoryId") Long categoryId,
                                        @Param("search") String search,
                                        Pageable pageable);
}
