package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.SalaryStructureComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryStructureComponentRepository extends JpaRepository<SalaryStructureComponent, Long> {

    @Query("SELECT ssc FROM SalaryStructureComponent ssc " +
           "JOIN FETCH ssc.salaryComponent sc " +
           "LEFT JOIN FETCH ssc.calculationBaseComponent " +
           "WHERE ssc.salaryStructure.id = :structureId " +
           "ORDER BY ssc.calculationOrder ASC, ssc.id ASC")
    List<SalaryStructureComponent> findBySalaryStructureIdOrderByCalculationOrderAsc(@Param("structureId") Long structureId);

    Optional<SalaryStructureComponent> findByIdAndSalaryStructureId(Long id, Long salaryStructureId);

    Optional<SalaryStructureComponent> findBySalaryStructureIdAndSalaryComponentId(Long salaryStructureId, Long salaryComponentId);

    boolean existsBySalaryStructureIdAndSalaryComponentId(Long salaryStructureId, Long salaryComponentId);

    boolean existsBySalaryStructureIdAndSalaryComponentIdAndIdNot(Long salaryStructureId, Long salaryComponentId, Long id);

    void deleteBySalaryStructureId(Long salaryStructureId);
}
