package org.lamisplus.modules.biometric.repository;

import org.lamisplus.modules.biometric.domain.Biometric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BiometricRepository extends JpaRepository<Biometric, String> {
    List<Biometric> findAllByPersonUuid(String personUuid);
}
