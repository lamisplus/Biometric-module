package org.lamisplus.modules.biometric.repository;

import org.lamisplus.modules.biometric.domain.BiometricDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BiometricDeviceRepository extends JpaRepository<BiometricDevice, Long> {
    List<BiometricDevice> getAllByActiveIsTrue();
    BiometricDevice findByActiveIsTrue();
    Optional<BiometricDevice> findByActive(Boolean active);
}
