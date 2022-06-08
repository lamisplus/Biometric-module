package org.lamisplus.modules.biometric.domain.dto;

import lombok.Data;

@Data
public class CapturedBiometricDto {
    private String templateType;
    private byte[] template;
}
