package org.lamisplus.modules.biometric.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
public class CaptureRequestDTO {
    //@NotNull(message = "patientId is mandatory")
    private Long patientId;

    //@NotBlank(message = "templateType is mandatory")
    private String templateType;

    @NotBlank(message = "biometricType is mandatory")
    private String biometricType;

    private String reason;

    private int age;
    private Long facilityId=0L;

    Set<CapturedBiometricDto> capturedBiometricsList = new HashSet<>();
}
