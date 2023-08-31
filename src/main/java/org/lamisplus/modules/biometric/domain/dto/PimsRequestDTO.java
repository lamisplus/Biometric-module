package org.lamisplus.modules.biometric.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PimsRequestDTO {
	private byte[] finger;
	private int index;
	private String facilityId;
	
}
