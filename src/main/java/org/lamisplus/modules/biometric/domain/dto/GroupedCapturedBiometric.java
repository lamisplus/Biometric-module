package org.lamisplus.modules.biometric.domain.dto;

public interface GroupedCapturedBiometric {
    Integer getCaptureDate();
    String getPersonUuid();
    Integer getRecapture();
    Integer getCount();
    Integer getArchived();
}
