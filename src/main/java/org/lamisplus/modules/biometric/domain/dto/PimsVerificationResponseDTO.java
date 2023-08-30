package org.lamisplus.modules.biometric.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PimsVerificationResponseDTO {
	private int code;
	private List<NdrPatientCurrentTreatmentDetail > enrollments;
	private String message;
	
}
