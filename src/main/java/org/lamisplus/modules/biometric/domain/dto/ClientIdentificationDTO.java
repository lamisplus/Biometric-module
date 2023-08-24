package org.lamisplus.modules.biometric.domain.dto;

import lombok.Data;

@Data
public class ClientIdentificationDTO {
    private String messageType;
    private String message;
    private String personUuid;
    private Long id;
    private String hospitalNumber;
    private String surname;
    private String otherName;
    private String firstName;
    private String sex;
}
