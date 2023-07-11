package org.lamisplus.modules.biometric.domain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CapturedBiometricDTOS {
    private LocalDate date;
    private int numberOfFingers;
    private Long personId;
    List<List<CapturedBiometricDto>> capturedBiometricsList2 = new ArrayList<>();
    List<CapturedBiometricDto> capturedBiometricsList = new ArrayList<>();
}
