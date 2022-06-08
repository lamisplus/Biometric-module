package org.lamisplus.modules.biometric.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.biometric.domain.Biometric;
import org.lamisplus.modules.biometric.domain.BiometricDevice;
import org.lamisplus.modules.biometric.domain.dto.BiometricDto;
import org.lamisplus.modules.biometric.domain.dto.BiometricEnrollmentDto;
import org.lamisplus.modules.biometric.domain.dto.DeviceDTO;
import org.lamisplus.modules.biometric.repository.BiometricDeviceRepository;
import org.lamisplus.modules.biometric.services.BiometricService;
import org.lamisplus.modules.biometric.services.SecugenManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BiometricController {
    private final BiometricService biometricService;
    private final BiometricDeviceRepository biometricDeviceRepository;
    //Versioning through URI Path
    private final String BASE_URL_VERSION_ONE = "/api/v1/biometrics";

    @PostMapping(BASE_URL_VERSION_ONE + "/templates")
    public ResponseEntity<BiometricDto> saveBiometric(@RequestBody BiometricEnrollmentDto biometrics) {
        return ResponseEntity.ok (biometricService.biometricEnrollment (biometrics));
    }
    @GetMapping(BASE_URL_VERSION_ONE + "/patient/{id}")
    public ResponseEntity<List<Biometric>> findByPatient(@PathVariable Long id) {
        return ResponseEntity.ok (biometricService.getByPersonId (id));
    }

    @PostMapping(BASE_URL_VERSION_ONE + "/device")
    public ResponseEntity<BiometricDevice> saveBiometric(@RequestBody BiometricDevice biometricDevice) {
        return ResponseEntity.ok (biometricDeviceRepository.save (biometricDevice));
    }

    @GetMapping(BASE_URL_VERSION_ONE + "/devices")
    public ResponseEntity<List<BiometricDevice>> getActiveBiometricDevice() {
        return ResponseEntity.ok (biometricDeviceRepository.getAllByActiveIsTrue ());
    }
}
