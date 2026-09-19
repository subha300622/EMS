package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.SalaryComponent;
import com.example.ems.payroll.entity.SalaryComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long> {

    List<SalaryComponent> findByOrganizationId(Long organizationId);

    List<SalaryComponent> findByOrganizationIdAndActive(Long organizationId, Boolean active);

    List<SalaryComponent> findByOrganizationIdAndComponentType(Long organizationId, SalaryComponentType componentType);

    List<SalaryComponent> findByOrganizationIdAndComponentTypeAndActive(Long organizationId, SalaryComponentType componentType, Boolean active);

    Optional<SalaryComponent> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<SalaryComponent> findByOrganizationIdAndCode(Long organizationId, String code);

    boolean existsByOrganizationIdAndCode(Long organizationId, String code);

    boolean existsByOrganizationIdAndCodeAndIdNot(Long organizationId, String code, Long id);
}
