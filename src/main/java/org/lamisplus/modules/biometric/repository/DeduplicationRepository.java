package org.lamisplus.modules.biometric.repository;

import org.lamisplus.modules.biometric.domain.Deduplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeduplicationRepository extends JpaRepository<Deduplication, Long> {
}
