package com.example.ems.settings.repository;

import com.example.ems.settings.entity.CompanySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanySettingRepository extends JpaRepository<CompanySetting, Long> {
}
