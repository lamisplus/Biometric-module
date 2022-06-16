package org.lamisplus.modules.biometric.services;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.biometric.domain.dto.*;
import org.lamisplus.modules.biometric.enumeration.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecugenService {
    private final SecugenManager secugenManager;

    public BiometricEnrollmentDto enrollment(String reader, CaptureRequestDTO captureRequestDTO){
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
        captureRequestDTO.getCapturedBiometricsList().forEach(capturedBiometricDto -> {
            BiometricStoreDTO.addCapturedBiometrics(captureRequestDTO.getPatientId(), capturedBiometricDto);
        });


        try {
            biometric = secugenManager.captureFingerPrint(biometric);
            AtomicReference<Boolean> matched = new AtomicReference<>(false);
            if (biometric.getTemplate().length > 200 && biometric.getImageQuality() >= 80) {
                byte[] scannedTemplate = biometric.getTemplate();
                if(biometric.getTemplate() != null && !BiometricStoreDTO.getPatientBiometricStore().isEmpty()) {
                    final List<CapturedBiometricDto> capturedBiometricsListDTO = BiometricStoreDTO
                            .getPatientBiometricStore()
                            .values()
                            .stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());

                    for (CapturedBiometricDto capturedBiometricsDTO : capturedBiometricsListDTO) {
                        matched.set(secugenManager.matchTemplate(capturedBiometricsDTO.getTemplate(),
                                biometric.getTemplate()));
                        if (matched.get()) {
                            //biometric.setCapturedBiometricsList(BiometricStoreDTO.getPatientBiometricStore().get(biometric.getPatientId()));
                            biometric.setCapturedBiometricsList(capturedBiometricsListDTO);
                            return this.addErrorMessage(biometric, "Fingerprint already captured");
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
                return this.addErrorMessage(biometric, null);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return this.addErrorMessage(biometric, exception.getMessage());
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

    private BiometricEnrollmentDto addErrorMessage(BiometricEnrollmentDto biometricEnrollmentDto, String customMessage){
        int imageQuality = biometricEnrollmentDto.getImageQuality();
        int templateLength = biometricEnrollmentDto.getTemplate().length;
        biometricEnrollmentDto.getMessage().put("ERROR", "ERROR WHILE CAPTURING... " +
                "\nImage Quality: " + (imageQuality < 80 ? "Bad - " + imageQuality : "Good - " + imageQuality) +
                "\nTemplate Length: " + (templateLength < 200 ? "Bad - " + templateLength : "Good - " + templateLength) +
                "\n" + (customMessage != null ? customMessage : "")
        );
        biometricEnrollmentDto.setType(BiometricEnrollmentDto.Type.ERROR);
        return biometricEnrollmentDto;
    }
}
