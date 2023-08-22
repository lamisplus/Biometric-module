package org.lamisplus.modules.biometric.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.biometric.domain.PimsConfig;
import org.lamisplus.modules.biometric.domain.dto.BiometricDto;
import org.lamisplus.modules.biometric.domain.dto.BiometricEnrollmentDto;
import org.lamisplus.modules.biometric.domain.dto.PimsRequestDTO;
import org.lamisplus.modules.biometric.domain.dto.PimsVerificationResponseDTO;
import org.lamisplus.modules.biometric.services.BiometricService;
import org.lamisplus.modules.biometric.services.PimsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.List;

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
	
	@PostMapping(BASE_URL_VERSION_ONE + "/config")
	public ResponseEntity<PimsConfig> registerConfig(
			@RequestParam  (name ="username") String username,
			@RequestParam(name = "password") String password,
			@RequestParam(name = "url") String url){
		return ResponseEntity.ok (pimsService.registerPimsConfig (username, password, url));
	}
	@PutMapping(BASE_URL_VERSION_ONE + "/config/{id}")
	public ResponseEntity<PimsConfig> updateConfig(
			@PathVariable("id") long id,
			@RequestBody PimsConfig config){
		return ResponseEntity.ok (pimsService.updatePimsConfig (id, config));
	}
	@GetMapping(BASE_URL_VERSION_ONE + "/config")
	public ResponseEntity<List<PimsConfig>> getConfig(){
		return ResponseEntity.ok (pimsService.getPimConfigs ());
	}
}
