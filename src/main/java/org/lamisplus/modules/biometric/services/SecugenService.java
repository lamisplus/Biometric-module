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
    private final SecugenManager secugenManager;
    private final BiometricRepository biometricRepository;
    private final CurrentUserOrganizationService facility;
    public BiometricEnrollmentDto enrollment(String reader, Boolean isNew, CaptureRequestDTO captureRequestDTO){
        if(isNew){
            this.emptyStoreByPersonId(captureRequestDTO.getPatientId());
        }
        //this.emptyStoreByPersonId(captureRequestDTO.getPatientId());

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

//            System.out.println("********************************************************");
//            System.out.println("firstTwoChar inside: "+firstTwoChar);
//            System.out.println("You convert?: "+template);
//            System.out.println("********************************************************");

            Set<StoredBiometric> biometricsInFacility = biometricRepository
                    .findByFacilityIdWithTemplate(facility
                            .getCurrentUserOrganization(), template);
            //System.out.println("biometricsInFacility size - "+biometricsInFacility.size());

            if(getMatch(biometricsInFacility, biometric.getTemplate())){
                return this.addMessage(biometric, "Fingerprint already captured");
            }

            captureRequestDTO.getCapturedBiometricsList().forEach(capturedBiometricDto -> {
                BiometricStoreDTO.addCapturedBiometrics(captureRequestDTO.getPatientId(), capturedBiometricDto);
            });

            //biometric = secugenManager.captureFingerPrint(biometric);
            AtomicReference<Boolean> matched = new AtomicReference<>(false);
            if (biometric.getTemplate().length > 200 && (biometric.getImageQuality() >= 61 || biometric.getAge() <= 6)) {
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
                            return this.addMessage(biometric, "Fingerprint already captured");
                        }
                    }
                } else {
                    biometric.setCapturedBiometricsList(new ArrayList<>());
                }
                biometric.getMessage().put("REGISTRATION", "PROCEEDING...");
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
                return this.addMessage(biometric, null);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return this.addMessage(biometric, exception.getMessage());
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
    private BiometricEnrollmentDto addMessage(BiometricEnrollmentDto biometricEnrollmentDto, String customMessage){
        int imageQuality = biometricEnrollmentDto.getImageQuality();
        int templateLength = biometricEnrollmentDto.getTemplate().length;
        biometricEnrollmentDto.getMessage().put("ERROR", "ERROR WHILE CAPTURING... " +
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
                matched = secugenManager.matchTemplate(biometric.getLeftIndexFinger(), scannedTemplate);
            } else if (biometric.getLeftMiddleFinger() != null && biometric.getLeftMiddleFinger().length != 0) {
                matched = secugenManager.matchTemplate(biometric.getLeftMiddleFinger(), scannedTemplate);
            } else if (biometric.getLeftThumb() != null && biometric.getLeftThumb().length != 0) {
                matched =  secugenManager.matchTemplate(biometric.getLeftThumb(), scannedTemplate);
            } else if (biometric.getLeftLittleFinger() != null && biometric.getLeftLittleFinger().length != 0) {
                matched = secugenManager.matchTemplate(biometric.getLeftLittleFinger(), scannedTemplate);
            } else if (biometric.getLeftRingFinger() != null && biometric.getLeftRingFinger().length != 0) {
                matched =  secugenManager.matchTemplate(biometric.getLeftRingFinger(), scannedTemplate);
            } else if (biometric.getRightIndexFinger() != null && biometric.getRightIndexFinger().length != 0) {
                matched =  secugenManager.matchTemplate(biometric.getRightIndexFinger(), scannedTemplate);
            } else if (biometric.getRightMiddleFinger() != null && biometric.getRightMiddleFinger().length != 0) {
                matched =  secugenManager.matchTemplate(biometric.getRightMiddleFinger(), scannedTemplate);
            } else if (biometric.getRightThumb() != null && biometric.getRightThumb().length != 0) {
                matched =  secugenManager.matchTemplate(biometric.getRightThumb(), scannedTemplate);
            } else if (biometric.getRightRingFinger() != null && biometric.getRightRingFinger().length != 0) {
                matched =  secugenManager.matchTemplate(biometric.getRightRingFinger(), scannedTemplate);
            } else if (biometric.getRightLittleFinger() != null && biometric.getRightLittleFinger().length != 0) {
                matched =  secugenManager.matchTemplate(biometric.getRightLittleFinger(), scannedTemplate);
            }

            if(matched)break;
        }
        return matched;
    }
}
