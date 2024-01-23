package org.lamisplus.modules.biometric.domain.dto;

import lombok.Data;

@Data
public class CapturedBiometricDto {
    private String Id;
    private String templateType;
    private byte[] template;
    private String hashed;
    private Integer imageQuality;
}
