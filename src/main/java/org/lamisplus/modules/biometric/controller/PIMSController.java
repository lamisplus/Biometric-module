package org.lamisplus.modules.biometric.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.biometric.domain.dto.BiometricDto;
import org.lamisplus.modules.biometric.domain.dto.BiometricEnrollmentDto;
import org.lamisplus.modules.biometric.domain.dto.PimsRequestDTO;
import org.lamisplus.modules.biometric.domain.dto.PimsVerificationResponseDTO;
import org.lamisplus.modules.biometric.services.BiometricService;
import org.lamisplus.modules.biometric.services.PimsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PIMSController {
	private final PimsService pimsService;
	private final String BASE_URL_VERSION_ONE = "/api/v1/pims";
	
	@PostMapping(BASE_URL_VERSION_ONE + "/verify/{facilityId}")
	public ResponseEntity<Object> saveBiometricTemplate(
			@PathVariable  long facilityId,
			@RequestParam(name = "patientId",  required = false) String patientId,
			@RequestBody PimsRequestDTO pimsRequestDTO){
		return ResponseEntity.ok (pimsService.verifyPatientFromPins (facilityId, patientId, pimsRequestDTO));
	}
}
