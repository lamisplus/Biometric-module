package org.lamisplus.modules.biometric.services;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.biometric.domain.dto.*;
import org.lamisplus.modules.biometric.enumeration.ErrorCode;
import org.lamisplus.modules.biometric.repository.BiometricRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecugenService {
    public static final String ERROR = "ERROR";
    public static final String WARNING = "WARNING";
    public static final String RECAPTURE_MESSAGE = "No baseline biometrics for recapturing";
    public static final String FINGERPRINT_ALREADY_CAPTURED = "Fingerprint already captured";
    public static final int IMAGE_QUALITY = 61;
    private final SecugenManager secugenManager;
    private final BiometricRepository biometricRepository;
    private final CurrentUserOrganizationService facility;
    public BiometricEnrollmentDto enrollment(String reader, Boolean isNew, Boolean recapture, CaptureRequestDTO captureRequestDTO){
        if(isNew){
            this.emptyStoreByPersonId(captureRequestDTO.getPatientId());
        }
        //this.emptyStoreByPersonId(captureRequestDTO.getPatientId());

        BiometricEnrollmentDto biometric = getBiometricEnrollmentDto(captureRequestDTO);
        if(biometric.getMessage() == null)biometric.setMessage(new HashMap<>());
        if (this.scannerIsNotSet(reader)) {
            biometric.getMessage().put(ERROR, "READER NOT AVAILABLE");
            biometric.setType(BiometricEnrollmentDto.Type.ERROR);
            return biometric;
        }
        biometric.setDeviceName(reader);
        biometric.getMessage().put("STARTED CAPTURING", "PROCEEDING...");
        Long error = secugenManager.boot(secugenManager.getDeviceId(reader));
        if (error > 0L) {
            ErrorCode errorCode = ErrorCode.getErrorCode(error);
            biometric.getMessage().put(ERROR, errorCode.getErrorName() + ": " + errorCode.getErrorMessage());
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

            //biometric = secugenManager.captureFingerPrint(biometric);
            AtomicReference<Boolean> matched = new AtomicReference<>(false);
            if (biometric.getTemplate().length > 200 && biometric.getImageQuality() >= IMAGE_QUALITY) {

                Set<StoredBiometric> biometricsInFacility = biometricRepository
                        .findByFacilityIdWithTemplate(facility.getCurrentUserOrganization(), template);

                //get match
                HashMap<String, Boolean> match = getMatch(biometricsInFacility, biometric.getTemplate());

                //check if there is a match
                if (match.containsValue(Boolean.TRUE)) {
                    //if recapture and same patient
                    if(recapture && match.containsKey(biometricRepository.getPersonUuid(captureRequestDTO.getPatientId()))){
                        //this.addMessage(ERROR, biometric, "Patient Fingerprint match");
                        biometric.setType(BiometricEnrollmentDto.Type.SUCCESS);
                    /*} else
                    if(recapture && !match.containsKey(biometricRepository.getPersonUuid(captureRequestDTO.getPatientId()))){
                        return this.addMessage(ERROR, biometric, "Fingerprint exist but not same patient");*/
                    } else if(!recapture) {
                        return this.addMessage(ERROR, biometric, FINGERPRINT_ALREADY_CAPTURED);
                    }
                } //recapture but no match
                else if (recapture && !match.containsValue(Boolean.TRUE)){

                    //TODO: Correct this
                    //biometric.setRecaptureMessage(RECAPTURE_MESSAGE);
                    //this.addMessage(WARNING, biometric, RECAPTURE_MESSAGE);
                }
                /*if (recapture){
                    biometric.setRecapture(recaptureId);
                }else {
                    biometric.setRecapture(recaptureId);
                }*/


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
                            //biometric.setCapturedBiometricsList(BiometricStoreDTO.getPatientBiometricStore().get(biometric.getPatientId()));
                            //biometric.setCapturedBiometricsList(capturedBiometricsListDTO);
                            return this.addMessage(ERROR, biometric, FINGERPRINT_ALREADY_CAPTURED);
                        }
                    }
                } else {
                    biometric.setCapturedBiometricsList(new ArrayList<>());
                }
                biometric.getMessage().put("REGISTRATION", "PROCEEDING...");
                if(biometric.getMessage().containsKey(WARNING)){
                    biometric.setType(BiometricEnrollmentDto.Type.WARNING);
                }else {
                    biometric.setType(BiometricEnrollmentDto.Type.SUCCESS);
                }
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
    private BiometricEnrollmentDto addMessage(String messageKey, BiometricEnrollmentDto biometricEnrollmentDto, String customMessage){
        int imageQuality = biometricEnrollmentDto.getImageQuality();
        int templateLength = biometricEnrollmentDto.getTemplate().length;
        biometricEnrollmentDto.getMessage().put(messageKey, "ERROR WHILE CAPTURING... " +
                "\nImage Quality: " + (imageQuality < IMAGE_QUALITY ? "Bad - " + imageQuality : "Good - " + imageQuality) +
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

    public HashMap<String, Boolean> getMatch(Set<StoredBiometric> storedBiometrics, byte[] scannedTemplate) {
        Boolean matched = Boolean.FALSE;
        String patientId="";
        HashMap<String, Boolean> map = new HashMap<>();
        try {
            for (StoredBiometric biometric : storedBiometrics) {
                patientId = biometric.getPatientId();
                if (biometric.getLeftIndexFinger() != null && biometric.getLeftIndexFinger().length != 0) {
                    matched = secugenManager.matchTemplate(biometric.getLeftIndexFinger(), scannedTemplate);
                } else if (biometric.getLeftMiddleFinger() != null && biometric.getLeftMiddleFinger().length != 0) {
                    matched = secugenManager.matchTemplate(biometric.getLeftMiddleFinger(), scannedTemplate);
                } else if (biometric.getLeftThumb() != null && biometric.getLeftThumb().length != 0) {
                    matched = secugenManager.matchTemplate(biometric.getLeftThumb(), scannedTemplate);
                } else if (biometric.getLeftLittleFinger() != null && biometric.getLeftLittleFinger().length != 0) {
                    matched = secugenManager.matchTemplate(biometric.getLeftLittleFinger(), scannedTemplate);
                } else if (biometric.getLeftRingFinger() != null && biometric.getLeftRingFinger().length != 0) {
                    matched = secugenManager.matchTemplate(biometric.getLeftRingFinger(), scannedTemplate);
                } else if (biometric.getRightIndexFinger() != null && biometric.getRightIndexFinger().length != 0) {
                    matched = secugenManager.matchTemplate(biometric.getRightIndexFinger(), scannedTemplate);
                } else if (biometric.getRightMiddleFinger() != null && biometric.getRightMiddleFinger().length != 0) {
                    matched = secugenManager.matchTemplate(biometric.getRightMiddleFinger(), scannedTemplate);
                } else if (biometric.getRightThumb() != null && biometric.getRightThumb().length != 0) {
                    matched = secugenManager.matchTemplate(biometric.getRightThumb(), scannedTemplate);
                } else if (biometric.getRightRingFinger() != null && biometric.getRightRingFinger().length != 0) {
                    matched = secugenManager.matchTemplate(biometric.getRightRingFinger(), scannedTemplate);
                } else if (biometric.getRightLittleFinger() != null && biometric.getRightLittleFinger().length != 0) {
                    matched = secugenManager.matchTemplate(biometric.getRightLittleFinger(), scannedTemplate);
                }

                if (matched) break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        map.put(patientId, matched);
        return map;
    }
}
