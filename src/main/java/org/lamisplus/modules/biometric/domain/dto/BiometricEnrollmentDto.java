package org.lamisplus.modules.biometric.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@Data
public class BiometricEnrollmentDto implements Serializable {
    @NotNull(message = "patientId is mandatory")
    private Long patientId;
    private HashMap<String, String> message;
    private byte[] template;
    private List<CapturedBiometricDto> capturedBiometricsList;
    @NotBlank(message = "templateType is mandatory")
    private String templateType;
    private String deviceName;
    @NotBlank(message = "biometricType is mandatory")
    private String biometricType;
    public enum Type {ERROR, SUCCESS}
    private Type type;
    private boolean iso;
    private int imageHeight;
    private int imageWeight;
    private int imageResolution;
    private int matchingScore;
    private int imageQuality;
    private byte[] image;
}
