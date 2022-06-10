package org.lamisplus.modules.biometric.services;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.biometric.domain.dto.*;
import org.lamisplus.modules.biometric.enumeration.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
        Long readerId = secugenManager.getDeviceId(reader);
        secugenManager.boot(readerId);
        if (secugenManager.getError() > 0L) {
            ErrorCode errorCode = ErrorCode.getErrorCode(secugenManager.getError());
            biometric.getMessage().put("ERROR", errorCode.getErrorName() + ": " + errorCode.getErrorMessage());
            return biometric;
        }

        try {
            biometric = secugenManager.captureFingerPrint(biometric);
            AtomicReference<Boolean> matched = new AtomicReference<>(false);
            if (biometric.getTemplate().length > 200 && biometric.getImageQuality() >= 80) {

                byte[] scannedTemplate = biometric.getTemplate();
                if(biometric.getTemplate() != null && !BiometricStoreDTO.getPatientBiometricStore().isEmpty()) {
                    final List<CapturedBiometricDto> capturedBiometricsList = BiometricStoreDTO.getPatientBiometricStore().values().stream().findFirst().get();

                    for (CapturedBiometricDto capturedBiometrics : capturedBiometricsList) {
                        matched.set(secugenManager.matchTemplate(capturedBiometrics.getTemplate(), biometric.getTemplate()));
                        if (matched.get()) {
                            //log.info("Fingerprint already exist");
                            biometric.getMessage().put("ERROR", "Fingerprint already captured");
                            biometric.setType(BiometricEnrollmentDto.Type.ERROR);
                            biometric.setCapturedBiometricsList(BiometricStoreDTO.getPatientBiometricStore().get(biometric.getPatientId()));
                            biometric.setCapturedBiometricsList(capturedBiometricsList);
                            return biometric;
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
                biometric.getMessage().put("ERROR", "COULD_NOT_CAPTURE_TEMPLATE...");
                biometric.setType(BiometricEnrollmentDto.Type.ERROR);
                return biometric;
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            biometric.getMessage().put("ERROR", exception.getMessage());
            biometric.getMessage().put("IMAGE QUALITY",
                    (biometric.getImageQuality() < 80) ? "LOW - " + biometric.getImageQuality() : "OK");

            biometric.getMessage().put("TEMPLATE LENGTH",
                    (biometric.getTemplate().length < 200) ? "LOW - " + biometric.getTemplate().length : "OK");
            biometric.setType(BiometricEnrollmentDto.Type.ERROR);
            return biometric;
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
}
