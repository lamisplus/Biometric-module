package org.lamisplus.modules.biometric.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.biometric.domain.Biometric;
import org.lamisplus.modules.biometric.domain.BiometricDevice;
import org.lamisplus.modules.biometric.domain.dto.BiometricDto;
import org.lamisplus.modules.biometric.domain.dto.BiometricEnrollmentDto;
import org.lamisplus.modules.biometric.repository.BiometricDeviceRepository;
import org.lamisplus.modules.biometric.services.BiometricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/patient/biometric")
public class BiometricController {
    private final BiometricService biometricService;
    private final BiometricDeviceRepository biometricDeviceRepository;

    @PostMapping("/templates")
    public ResponseEntity<BiometricDto> saveBiometric(@RequestBody BiometricEnrollmentDto biometrics) {
        return ResponseEntity.ok (biometricService.biometricEnrollment (biometrics));
    }
    @GetMapping("/patient/{id}")
    public ResponseEntity<List<Biometric>> findByPatient(@PathVariable Long id) {
        return ResponseEntity.ok (biometricService.getByPersonId (id));
    }

    @PostMapping("/device")
    public ResponseEntity<BiometricDevice> saveBiometric(@RequestBody BiometricDevice biometricDevice) {
        return ResponseEntity.ok (biometricDeviceRepository.save (biometricDevice));
    }

    @GetMapping("/devices")
    public ResponseEntity<List<BiometricDevice>> getActiveBiometricDevice() {
        return ResponseEntity.ok (biometricDeviceRepository.getAllByActiveIsTrue ());
    }


}
