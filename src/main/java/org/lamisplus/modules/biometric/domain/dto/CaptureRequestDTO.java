package org.lamisplus.modules.biometric.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


@Data
public class CaptureRequestDTO {
    @NotNull(message = "patientId is mandatory")
    private Long patientId;

    @NotBlank(message = "templateType is mandatory")
    private String templateType;

    @NotBlank(message = "biometricType is mandatory")
    private String biometricType;

    List<CapturedBiometricDto> capturedBiometricsList = new ArrayList<>();
}
