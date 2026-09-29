package com.example.ems.support.repository;

import com.example.ems.support.entity.SupportWorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SupportWorkLogRepository extends JpaRepository<SupportWorkLog, Long> {
    List<SupportWorkLog> findByTicketIdOrderByStartedAtAsc(Long ticketId);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM SupportWorkLog w " +
           "WHERE w.engineer.id = :engineerId " +
           "AND w.startedAt < :endedAt AND w.endedAt > :startedAt")
    boolean existsOverlappingWorkLog(@Param("engineerId") Long engineerId,
                                    @Param("startedAt") LocalDateTime startedAt,
                                    @Param("endedAt") LocalDateTime endedAt);
}
