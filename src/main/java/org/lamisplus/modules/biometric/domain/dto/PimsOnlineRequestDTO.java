package org.lamisplus.modules.biometric.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PimsOnlineRequestDTO {
	private String finger;
	private int index;
	private String facilityId;
	
}
