package org.lamisplus.modules.biometric.domain.dto;

import java.time.LocalDate;

public interface GroupedCapturedBiometric {
    LocalDate getCaptureDate();
    String getPersonUuid();
    Integer getRecapture();
    Integer getCount();
    Integer getArchived();
    LocalDate getReplaceDate();
}
