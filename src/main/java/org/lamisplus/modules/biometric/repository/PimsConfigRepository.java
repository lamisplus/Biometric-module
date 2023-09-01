package org.lamisplus.modules.biometric.repository;

import org.lamisplus.modules.biometric.domain.PimsConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PimsConfigRepository extends JpaRepository<PimsConfig, Long> {
}