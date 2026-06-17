package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.TaxSlab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxSlabRepository extends JpaRepository<TaxSlab, Long> {
    List<TaxSlab> findByRegime(String regime);
}
