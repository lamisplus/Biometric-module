package org.lamisplus.modules.biometric.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PimsUserCredentials {
	private String email;
	private String password;
	
}
