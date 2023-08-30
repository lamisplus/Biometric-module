package org.lamisplus.modules.biometric.repository;

import org.lamisplus.modules.biometric.domain.PimsTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PimsTrackerRepository extends JpaRepository<PimsTracker, String> {
	Optional<PimsTracker> getPimsTrackerByPersonUuidAndFacilityIdAndArchived(String uuid, Long facilityId, int archived);
}