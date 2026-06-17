package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.PayrollSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollSettingRepository extends JpaRepository<PayrollSetting, Long> {
    Optional<PayrollSetting> findBySettingKey(String settingKey);
}
