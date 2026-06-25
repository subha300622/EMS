package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitKtContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExitKtContactRepository extends JpaRepository<ExitKtContact, Long> {
}
