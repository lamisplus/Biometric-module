package org.lamisplus.modules.biometric.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.biometric.domain.PimsTracker;
import org.lamisplus.modules.biometric.domain.dto.PimsAuthenticationResponse;
import org.lamisplus.modules.biometric.domain.dto.PimsRequestDTO;
import org.lamisplus.modules.biometric.domain.dto.PimsUserCredentials;
import org.lamisplus.modules.biometric.domain.dto.PimsVerificationResponseDTO;
import org.lamisplus.modules.biometric.repository.PimsTrackerRepository;
import org.lamisplus.modules.patient.controller.exception.AlreadyExistException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PimsService {
	
	private final PimsTrackerRepository pimsTrackerRepository;
	public PimsVerificationResponseDTO verifyPatientFromPins(Long facilityId,String patientId, PimsRequestDTO pimsRequestDTO) {
		LOG.info("id {}", patientId);
		ObjectMapper mapper = new ObjectMapper();
		PimsVerificationResponseDTO pimsVerificationResponseDTO = patientISAlreadyPIMSVerified(facilityId, patientId,mapper);
		if(pimsVerificationResponseDTO != null){
			return pimsVerificationResponseDTO;
		}
		RestTemplate restTemplate = new RestTemplate();
		PimsAuthenticationResponse pimsAuthentication = getPimsAuthentication(restTemplate);
		String url = "http://stagedemo.phis3project.org.ng/pims/findPatient";
			if (pimsAuthentication != null && pimsAuthentication.getIsAuthenticated().equalsIgnoreCase("true")) {
				String token = pimsAuthentication.getToken();
				pimsRequestDTO.setToken(token);
				HttpEntity<PimsRequestDTO> requestDTOEntity = new HttpEntity<>(pimsRequestDTO, GetHTTPHeaders());
				ResponseEntity<PimsVerificationResponseDTO> responseEntity =
						getRestTemplate(restTemplate).exchange(url, HttpMethod.POST, requestDTOEntity, PimsVerificationResponseDTO.class);
				PimsVerificationResponseDTO response = responseEntity.getBody();
				LOG.info("verify Response: " + response);
				saveVerificationOnLocalSystem(facilityId, patientId, mapper, response);
				return responseEntity.getBody();
			}else {
				LOG.error("Failed authentication from PIMS server, kindly ensure you had valid credentials");
				return null;
			}
	}
	
	private void saveVerificationOnLocalSystem(Long facilityId, String patientId, ObjectMapper mapper, PimsVerificationResponseDTO response) {
		JsonNode jsonNodeResponse = mapper.valueToTree(response);
		LOG.info("saving Response on system ");
		PimsTracker pimsTracker = PimsTracker.builder()
				.isVerified(response.getMessage().contains("success"))
				.facilityId(facilityId)
				.data(jsonNodeResponse)
				.pimsPatientId(response.getEnrollments().get(0).getPatientId())
				.personUuid(patientId)
				.date(LocalDate.now())
				.build();
		pimsTrackerRepository.save(pimsTracker);
		LOG.info("save successfully");
	}
	
	private PimsVerificationResponseDTO patientISAlreadyPIMSVerified(Long facilityId, String patientId, ObjectMapper mapper) {
		try {
			if (patientId != null) {
				Optional<PimsTracker> pimsTrackerOptional = pimsTrackerRepository.getPimsTrackerByPersonUuidAndFacilityId(patientId, facilityId);
				if (pimsTrackerOptional.isPresent()) {
					PimsTracker pimsTracker = pimsTrackerOptional.get();
					if (pimsTracker.getIsVerified()) {
						JsonNode data = pimsTracker.getData();
						return mapper.treeToValue(data, PimsVerificationResponseDTO.class);
					}
				}
			}
		}catch(Exception e){
		  LOG.error("An error during authentication error {} ", Arrays.toString(e.getStackTrace()) );
		}
		return null;
	}
	
	public PimsAuthenticationResponse getPimsAuthentication(RestTemplate restTemplate) {
		try {
			String url = "http://stagedemo.phis3project.org.ng/pims/auth";
			PimsUserCredentials userCredentials = new PimsUserCredentials("test_user", "test_user_password");
			HttpEntity<PimsUserCredentials> loginEntity = new HttpEntity<>(userCredentials, GetHTTPHeaders());
			ResponseEntity<PimsAuthenticationResponse> responseEntity =
					getRestTemplate(restTemplate).exchange(url, HttpMethod.POST, loginEntity, PimsAuthenticationResponse.class);
			LOG.info("auth response {}", responseEntity.getBody());
			return responseEntity.getBody();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private RestTemplate getRestTemplate(RestTemplate restTemplate) {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
		messageConverters.add(converter);
		restTemplate.setMessageConverters(messageConverters);
		return restTemplate;
	}
	
	private HttpHeaders GetHTTPHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("user-agent", "Application");
		return headers;
	}
}
