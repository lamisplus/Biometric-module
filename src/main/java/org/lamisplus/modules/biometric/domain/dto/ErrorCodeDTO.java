package org.lamisplus.modules.biometric.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.lamisplus.modules.biometric.enumeration.ErrorCode;

@Data
@Builder
public class ErrorCodeDTO {
    private final Long errorID;
    private final String errorName;
    private final String errorMessage;
    private final ErrorCode.Type errorType;
}
