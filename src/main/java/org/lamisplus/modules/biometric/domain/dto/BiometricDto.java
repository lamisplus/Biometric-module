package org.lamisplus.modules.biometric.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lamisplus.modules.patient.utility.LocalDateConverter;

import javax.persistence.Convert;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricDto implements Serializable {
    private Long personId;
    private Integer numberOfFingers;
    @NotNull
    @Convert(converter = LocalDateConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Boolean iso;
    private String matchBiometricId;
}
