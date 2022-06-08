package org.lamisplus.modules.biometric.domain.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BiometricStoreDTO {
    //public static List<CapturedBiometrics> capturedBiometrics;
    private static HashMap<Long, List<CapturedBiometricDto>> patientBiometricStore;


    public static HashMap<Long, List<CapturedBiometricDto>> addCapturedBiometrics(Long patientId, CapturedBiometricDto capturedBiometric){
        if(patientBiometricStore == null){
            ArrayList<CapturedBiometricDto> capturedBiometrics = new ArrayList<>();
            patientBiometricStore = new HashMap<>();
            capturedBiometrics.add(capturedBiometric);
            patientBiometricStore.put(patientId, capturedBiometrics );
            return patientBiometricStore;
        }
        if(!BiometricStoreDTO.patientBiometricStore.containsKey(patientId)){
            BiometricStoreDTO.patientBiometricStore = null;
            addCapturedBiometrics(patientId, capturedBiometric);
        }
        patientBiometricStore.get(patientId).add(capturedBiometric);
        return patientBiometricStore;
    }

    public static HashMap<Long, List<CapturedBiometricDto>> getPatientBiometricStore(){
        if(patientBiometricStore == null){
            return new HashMap<Long, List<CapturedBiometricDto>>();
        }
        return patientBiometricStore;
    }
}
