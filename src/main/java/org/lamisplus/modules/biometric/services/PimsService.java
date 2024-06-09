package org.lamisplus.modules.biometric.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.base.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.base.domain.entities.OrganisationUnitIdentifier;
import org.lamisplus.modules.base.domain.repositories.OrganisationUnitIdentifierRepository;
import org.lamisplus.modules.biometric.domain.PimsConfig;
import org.lamisplus.modules.biometric.domain.PimsTracker;
import org.lamisplus.modules.biometric.domain.dto.*;
import org.lamisplus.modules.biometric.repository.PimsConfigRepository;
import org.lamisplus.modules.biometric.repository.PimsTrackerRepository;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PimsService {
	
	//public static final String HTTP_STAGEDEMO_PHIS_3_PROJECT_ORG_NG_PIMS = "http://stagedemo.phis3project.org.ng/pims";
	//public static final String HTTP_STAGEDEMO_PHIS_3_PROJECT_ORG_NG_PIMS = "http://pimssandbox.phis3project.org.ng/api";

	public static final String HTTP_STAGEDEMO_PHIS_3_PROJECT_ORG_NG_PIMS = "*****";
	private final PimsTrackerRepository pimsTrackerRepository;
	private final PimsConfigRepository pimsConfigRepository;
	
	private final OrganisationUnitIdentifierRepository identifierRepository;
	
	
	public PimsConfig registerPimsConfig(String username, String password, String url){
		return  pimsConfigRepository.save(new PimsConfig(username, password, url));
	}
	
	public PimsConfig updatePimsConfig(Long id, PimsConfig pimsConfig){
		PimsConfig pimConfig = pimsConfigRepository
				.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(PimsConfig.class, "id", id + " not found"));
		pimConfig.setUrl(pimsConfig.getUrl());
		pimConfig.setUsername(pimsConfig.getUsername());
		pimConfig.setPassword(pimsConfig.getPassword());
		return  pimsConfigRepository.save(pimConfig);
	}
	
	public List<PimsTracker> getAllPimsVerification(){
		return  pimsTrackerRepository.findAll()
				.stream()
				.filter(p->p.getArchived() == 0)
				.collect(Collectors.toList());
	}
	public List<PimsTracker> getPassedPimsVerification(){
		return  pimsTrackerRepository.findAll()
				.stream()
				.filter(p->p.getArchived() == 0 && p.getIsVerified())
				.collect(Collectors.toList());
	}
	
	public List<PimsTracker> getFailedPimsVerification(){
		return  pimsTrackerRepository.findAll()
				.stream()
				.filter(p->p.getArchived() == 0 && !p.getIsVerified())
				.collect(Collectors.toList());
	}
	
	public List<PimsConfig> getPimConfigs(){
		return  pimsConfigRepository.findAll().stream().filter(pimsConfig -> pimsConfig.getArchived()==0)
				.collect(Collectors.toList());
	}
	
	public Object verifyPatientFromPins(Long facilityId,String patientId, PimsRequestDTO pimsRequestDTO) {
		LOG.info("id {}", patientId);
		LOG.info("facility {}", facilityId);
		ObjectMapper mapper = new ObjectMapper();
		PimsVerificationResponseDTO pimsVerificationResponseDTO = patientISAlreadyPIMSVerified(facilityId, patientId,mapper);
		if(pimsVerificationResponseDTO != null){
			return pimsVerificationResponseDTO;
		}
		Optional<OrganisationUnitIdentifier> datimIdOptional = identifierRepository.findAll()
				.stream()
				.filter(i -> i.getOrganisationUnitId().equals(facilityId) && i.getName().equals("DATIM_ID"))
				.findAny();
		if(datimIdOptional.isPresent()){
			OrganisationUnitIdentifier organisationUnitIdentifier = datimIdOptional.get();
			pimsRequestDTO.setFacilityId(organisationUnitIdentifier.getCode());
			LOG.info("datim code {}", organisationUnitIdentifier.getCode());
		}
		RestTemplate restTemplate = new RestTemplate();
		//String url = "http://pimssandbox.phis3project.org.ng/api/Prints/findClient";

		String url = "https://pimssandbox.phis3project.org.ng/api/Prints/findClient";
		PimsAuthenticationResponse pimsAuthentication = getPimsAuthentication(restTemplate);
		Optional<PimsConfig> config = pimsConfigRepository.findAll().stream()
				.filter(c -> c.getArchived() == 0)
				.findAny();
		if(config.isPresent()){
			LOG.info("dynamic configuration");
			url = config.get().getUrl()+"/Prints/findClient";
		}
			if (pimsAuthentication != null && pimsAuthentication.getIsAuthenticated().equalsIgnoreCase("true")) {
				String token = pimsAuthentication.getToken();
				LOG.info("token: " + token);
				HttpHeaders headers = GetHTTPHeaders();
				headers.add("Authorization","Bearer "+token);
				PimsOnlineRequestDTO request = new PimsOnlineRequestDTO();
				request.setIndex(pimsRequestDTO.getIndex());
				request.setFacilityId(pimsRequestDTO.getFacilityId());
				request.setFinger(Base64.getEncoder().encodeToString(pimsRequestDTO.getFinger()));
				HttpEntity<PimsOnlineRequestDTO> requestDTOEntity = new HttpEntity<>(request, headers);
				ResponseEntity<PimsVerificationResponseDTO> responseEntity =
						getRestTemplate(restTemplate).exchange(url, HttpMethod.POST, requestDTOEntity, PimsVerificationResponseDTO.class);
				PimsVerificationResponseDTO response = responseEntity.getBody();
				LOG.info("verify Response: " + response);
				saveVerificationOnLocalSystem(facilityId, patientId, mapper, response);
				return responseEntity.getBody();
			}else {
				LOG.error("Failed authentication from PIMS server, kindly ensure you had valid credentials");
				return  pimsAuthentication;
			}
	}
	
	private void saveVerificationOnLocalSystem(Long facilityId, String patientId, ObjectMapper mapper, PimsVerificationResponseDTO response) {
		JsonNode jsonNodeResponse = mapper.valueToTree(response);
		LOG.info("saving Response on system ");
		String pimPatientId = null;
		if(!response.getEnrollments().isEmpty()){
			pimPatientId = response.getEnrollments().get(0)

					.getPatientId();
		}
		Optional<PimsTracker> pimsTrackerOptional =
				pimsTrackerRepository.getPimsTrackerByPersonUuidAndFacilityIdAndArchived(patientId, facilityId,0);
		if(pimsTrackerOptional.isPresent()){
			PimsTracker pimsTracker = pimsTrackerOptional.get();
			pimsTracker.setArchived(0);
			pimsTracker.setData(jsonNodeResponse);
			pimsTracker.setDate(LocalDate.now());
			pimsTracker.setPimsPatientId(pimPatientId);
			pimsTracker.setIsVerified(response.getMessage().contains("success"));
			pimsTrackerRepository.save(pimsTracker);
			LOG.info("updated successfully");
		}else {
			PimsTracker pimsTracker = PimsTracker.builder()
					.isVerified(response.getMessage().contains("success"))
					.facilityId(facilityId)
					.data(jsonNodeResponse)
					.pimsPatientId(pimPatientId)
					.personUuid(patientId)
					.archived(0)
					.date(LocalDate.now())
					.build();
			pimsTrackerRepository.save(pimsTracker);
			LOG.info("save successfully");
		}
	}
	
	private PimsVerificationResponseDTO patientISAlreadyPIMSVerified(Long facilityId, String patientId, ObjectMapper mapper) {
		try {
			if (patientId != null) {
				LOG.info("An already existed verified patient " );
				Optional<PimsTracker> pimsTrackerOptional =
						pimsTrackerRepository.getPimsTrackerByPersonUuidAndFacilityIdAndArchived(patientId, facilityId,0);
				if (pimsTrackerOptional.isPresent()) {
					PimsTracker pimsTracker = pimsTrackerOptional.get();
					LOG.info("data {}",  pimsTracker.toString());
					if (pimsTracker.getIsVerified()) {
						JsonNode data = pimsTracker.getData();
						return mapper.treeToValue(data, PimsVerificationResponseDTO.class);
					}
				}
			}
		}catch(Exception e){
		  LOG.error("An error occur during  checking a patient in the DB error message {} ", Arrays.toString(e.getStackTrace()) );
		}
		return null;
	}
	
	public PimsAuthenticationResponse getPimsAuthentication(RestTemplate restTemplate) {
		try {
			Optional<PimsConfig> config = pimsConfigRepository.findAll().stream()
					.filter(c -> c.getArchived() == 0)
					.findAny();
			String url = "https://pimssandbox.phis3project.org.ng/api/auth/token";
			PimsUserCredentials userCredentials = null;
			if(config.isPresent()){
				PimsConfig pimsConfig = config.get();
				url = pimsConfig.getUrl()+"/auth/token";
				LOG.info("dynamic coded configuration");
				userCredentials = new PimsUserCredentials(pimsConfig.getUsername(), pimsConfig.getPassword());
				
			}else{
				 userCredentials = new PimsUserCredentials("******", "******");
				LOG.info("payload: " + userCredentials.toString());
			}
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
	
//	public String bcryptHash(byte[] template) {
//		String encoded = Base64.getEncoder().encodeToString(template);
//		return BCrypt.hashpw(encoded, "$2a$12$MklNDNgs4Agd50cSasj91O");
//	}

}
