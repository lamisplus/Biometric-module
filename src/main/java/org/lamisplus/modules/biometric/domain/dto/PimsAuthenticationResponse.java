package org.lamisplus.modules.biometric.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PimsAuthenticationResponse {
	private String code;
	private String isAuthenticated;
	private String token;
	private List<Object> errors;
}
