package org.lamisplus.modules.biometric.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.biometric.domain.dto.*;
import org.lamisplus.modules.biometric.enumeration.ErrorCode;
import org.lamisplus.modules.biometric.repository.BiometricRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecugenService {
    public static final String ERROR = "ERROR";
    public static final int PAGE_SIZE = 2000;
    public static Integer totalPage=0;
    public static final String WARNING = "WARNING";
    public static final String RECAPTURE_MESSAGE = "No baseline biometrics for recapturing";
    public static final String FINGERPRINT_ALREADY_CAPTURED = "Fingerprint already captured";
    public static final int IMAGE_QUALITY = 61;
    private final SecugenManager secugenManager;
    private final BiometricRepository biometricRepository;
    private final CurrentUserOrganizationService facility;
    public static String MATCHED_PERSON_UUID;
    public BiometricEnrollmentDto enrollment(String reader, Boolean isNew, Boolean recapture, CaptureRequestDTO captureRequestDTO){
        if(isNew){
            this.emptyStoreByPersonId(captureRequestDTO.getPatientId());
        }

        BiometricEnrollmentDto biometric = getBiometricEnrollmentDto(captureRequestDTO);
        if(biometric.getMessage() == null)biometric.setMessage(new HashMap<>());
        if (this.scannerIsNotSet(reader)) {
            biometric.getMessage().put("ERROR", "READER NOT AVAILABLE");
            biometric.setType(BiometricEnrollmentDto.Type.ERROR);
            return biometric;
        }
        biometric.setDeviceName(reader);
        biometric.getMessage().put("STARTED CAPTURING", "PROCEEDING...");
        Long error = secugenManager.boot(secugenManager.getDeviceId(reader));
        if (error > 0L) {
            ErrorCode errorCode = ErrorCode.getErrorCode(error);
            biometric.getMessage().put("ERROR", errorCode.getErrorName() + ": " + errorCode.getErrorMessage());
            return biometric;
        }

        try {
            biometric = secugenManager.captureFingerPrint(biometric);
            byte firstTwoChar = biometric.getTemplate()[0];
            //String template = "46% OR AC%";
            String template = Integer.toHexString(firstTwoChar)+"%";

            captureRequestDTO.getCapturedBiometricsList().forEach(capturedBiometricDto -> {
                BiometricStoreDTO.addCapturedBiometrics(captureRequestDTO.getPatientId(), capturedBiometricDto);
            });

            AtomicReference<Boolean> matched = new AtomicReference<>(false);
            if (biometric.getTemplate().length > 200 && biometric.getImageQuality() >= IMAGE_QUALITY ) {

                /*Set<StoredBiometric> biometricsInFacility = biometricRepository
                        .findByFacilityIdWithTemplate(facility.getCurrentUserOrganization(), template);

                Boolean match = getMatch(biometricsInFacility, biometric.getTemplate());*/
                Boolean match = pageTemplate(template, biometric.getTemplate());
                Optional<String> optionalPersonUuid = biometricRepository.getPersonUuid(captureRequestDTO.getPatientId());

                if (match) {
                    //LOG.info("matched {}", match);
                    if(recapture) {
                        //if recapture and different patient
                        if (MATCHED_PERSON_UUID != null && !MATCHED_PERSON_UUID.equals(optionalPersonUuid.get())) {
                            this.addMessage(WARNING, biometric, "Fingerprint exist but not same patient");
                        }
                    }
                    if (!recapture) {
                        return this.addMessage(ERROR, biometric, FINGERPRINT_ALREADY_CAPTURED);
                    }
                }
                //recapture but no match found
                if(recapture && !match) {
                    this.addMessage(WARNING, biometric, RECAPTURE_MESSAGE);
                }


                byte[] scannedTemplate = biometric.getTemplate();
                if(biometric.getTemplate() != null && !BiometricStoreDTO.getPatientBiometricStore().isEmpty()) {
                    final List<CapturedBiometricDto> capturedBiometricsListDTO = BiometricStoreDTO
                            .getPatientBiometricStore()
                            .values()
                            .stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());

                    for (CapturedBiometricDto capturedBiometricsDTO : capturedBiometricsListDTO) {
                        matched.set(secugenManager.matchTemplate(capturedBiometricsDTO.getTemplate(), biometric.getTemplate()));
                        if (matched.get()) {
                            return this.addMessage(ERROR, biometric, "Fingerprint already captured");
                        }
                    }
                } else {
                    biometric.setCapturedBiometricsList(new ArrayList<>());
                }
                biometric.getMessage().put("CAPTURING", "PROCEEDING...");
                biometric.setType(BiometricEnrollmentDto.Type.SUCCESS);
                CapturedBiometricDto capturedBiometrics = new CapturedBiometricDto();
                capturedBiometrics.setTemplate(scannedTemplate);
                capturedBiometrics.setTemplateType(biometric.getTemplateType());

                List<CapturedBiometricDto> capturedBiometricsList =
                        BiometricStoreDTO.addCapturedBiometrics(biometric.getPatientId(), capturedBiometrics)
                                .get(biometric.getPatientId());

                biometric.setCapturedBiometricsList(capturedBiometricsList);
                biometric.setTemplate(scannedTemplate);
            }else {
                return this.addMessage(ERROR, biometric, null);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return this.addMessage(ERROR, biometric, exception.getMessage());
        }
        return biometric;
    }
    private boolean scannerIsNotSet(String reader) {
        Long readerId = secugenManager.getDeviceId(reader);
        for (DeviceDTO deviceDTO : secugenManager.getDevices()) {
            if (deviceDTO.getId().equals(String.valueOf(readerId))) {
                secugenManager.boot(readerId);
                return false;
            }
        }
        return true;
    }
    public ErrorCodeDTO boot(String reader) {
        ErrorCode errorCode = ErrorCode.getErrorCode(secugenManager.boot(secugenManager.getDeviceId(reader)));
        return ErrorCodeDTO.builder()
                .errorID(errorCode.getErrorID())
                .errorName(errorCode.getErrorName())
                .errorMessage(errorCode.getErrorMessage())
                .errorType(errorCode.getType())
                .build();
    }
    public BiometricEnrollmentDto getBiometricEnrollmentDto(CaptureRequestDTO captureRequestDTO){
        BiometricEnrollmentDto biometricEnrollmentDto = new BiometricEnrollmentDto();
        biometricEnrollmentDto.setBiometricType(captureRequestDTO.getBiometricType());
        biometricEnrollmentDto.setTemplateType(captureRequestDTO.getTemplateType());
        biometricEnrollmentDto.setPatientId(captureRequestDTO.getPatientId());
        return biometricEnrollmentDto;
    }
    private BiometricEnrollmentDto addMessage(String messageKey,BiometricEnrollmentDto biometricEnrollmentDto, String customMessage){
        int imageQuality = biometricEnrollmentDto.getImageQuality();
        int templateLength = biometricEnrollmentDto.getTemplate().length;
        biometricEnrollmentDto.getMessage().put(messageKey, "ERROR WHILE CAPTURING... " +
                "\nImage Quality: " + (imageQuality < 65 ? "Bad - " + imageQuality : "Good - " + imageQuality) +
                "\nTemplate Length: " + (templateLength < 200 ? "Bad - " + templateLength : "Good - " + templateLength) +
                "\n" + (customMessage != null ? customMessage : "")
        );
        biometricEnrollmentDto.setType(BiometricEnrollmentDto.Type.ERROR);
        return biometricEnrollmentDto;
    }
    public Boolean emptyStoreByPersonId(Long personId){
        Boolean hasCleared = false;
        if(!BiometricStoreDTO.getPatientBiometricStore().isEmpty() && BiometricStoreDTO.getPatientBiometricStore().get(personId) != null){
            BiometricStoreDTO.getPatientBiometricStore().remove(personId);
            hasCleared = true;
        }
        return hasCleared;
    }

    public Boolean getMatch(Set<StoredBiometric> storedBiometrics, byte[] scannedTemplate) {
        Boolean matched = Boolean.FALSE;
        for (StoredBiometric biometric : storedBiometrics) {
            if (biometric.getLeftIndexFinger() != null && biometric.getLeftIndexFinger().length != 0) {
                matched = setMatch(biometric.getLeftIndexFinger(), scannedTemplate, biometric.getPersonUuid());
            } else if (biometric.getLeftMiddleFinger() != null && biometric.getLeftMiddleFinger().length != 0) {
                matched = setMatch(biometric.getLeftMiddleFinger(), scannedTemplate, biometric.getPersonUuid());
            } else if (biometric.getLeftThumb() != null && biometric.getLeftThumb().length != 0) {
                matched = setMatch(biometric.getLeftThumb(), scannedTemplate, biometric.getPersonUuid());
            } else if (biometric.getLeftLittleFinger() != null && biometric.getLeftLittleFinger().length != 0) {
                matched = setMatch(biometric.getLeftLittleFinger(), scannedTemplate, biometric.getPersonUuid());
            } else if (biometric.getLeftRingFinger() != null && biometric.getLeftRingFinger().length != 0) {
                matched = setMatch(biometric.getLeftRingFinger(), scannedTemplate, biometric.getPersonUuid());
            } else if (biometric.getRightIndexFinger() != null && biometric.getRightIndexFinger().length != 0) {
                matched = setMatch(biometric.getRightIndexFinger(), scannedTemplate, biometric.getPersonUuid());
            } else if (biometric.getRightMiddleFinger() != null && biometric.getRightMiddleFinger().length != 0) {
                matched = setMatch(biometric.getRightMiddleFinger(), scannedTemplate, biometric.getPersonUuid());
            } else if (biometric.getRightThumb() != null && biometric.getRightThumb().length != 0) {
                matched = setMatch(biometric.getRightThumb(), scannedTemplate, biometric.getPersonUuid());
            } else if (biometric.getRightRingFinger() != null && biometric.getRightRingFinger().length != 0) {
                matched = setMatch(biometric.getRightRingFinger(), scannedTemplate, biometric.getPersonUuid());
            } else if (biometric.getRightLittleFinger() != null && biometric.getRightLittleFinger().length != 0) {
                matched = setMatch(biometric.getRightLittleFinger(), scannedTemplate, biometric.getPersonUuid());
            }
            if (matched) break;
        }
        return matched;
    }

    private Boolean setMatch(byte[] capturedFinger, byte[] dbPrint, String personUuid){
        MATCHED_PERSON_UUID = personUuid;
        return secugenManager.matchTemplate(capturedFinger, dbPrint);
    }

    /**
     * Pages request for template to the database
     * @param firstTwoChar
     * @param template
     * @return a Boolean for match
     */
    private Boolean pageTemplate(String firstTwoChar, byte[] template){
        Boolean match=Boolean.FALSE;
        for(int pageNo=0; pageNo <= totalPage; ++pageNo) {
            if (totalPage == 0) {
                Pageable pageable = PageRequest.of(pageNo, PAGE_SIZE);
                Page<StoredBiometric> biometricsInFacility = biometricRepository
                        .findByFacilityIdWithTemplate(facility.getCurrentUserOrganization(), firstTwoChar, pageable);
                totalPage = biometricsInFacility.getTotalPages();
                match = getMatch(biometricsInFacility.toSet(), template);
            } else {
                Page<StoredBiometric> biometricsInFacility = biometricRepository
                        .findByFacilityIdWithTemplate(facility.getCurrentUserOrganization(), firstTwoChar, PageRequest.of(pageNo, PAGE_SIZE));
                match = getMatch(biometricsInFacility.toSet(), template);
            }
            if(match)break;
        }
        totalPage=0;
        return match;

    }
}