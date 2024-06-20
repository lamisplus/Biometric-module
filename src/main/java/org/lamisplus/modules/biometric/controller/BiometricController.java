package org.lamisplus.modules.biometric.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.biometric.domain.Biometric;
import org.lamisplus.modules.biometric.domain.BiometricDevice;
import org.lamisplus.modules.biometric.domain.dto.*;
import org.lamisplus.modules.biometric.services.BiometricService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BiometricController {
    private final BiometricService biometricService;
    //Versioning through URI Path
    private final String BASE_URL_VERSION_ONE = "/api/v1/biometrics";
    @PostMapping(BASE_URL_VERSION_ONE + "/templates")
    public ResponseEntity<BiometricDto> saveBiometricTemplate(@RequestBody BiometricEnrollmentDto biometrics,
                                                              @RequestParam (required = false, defaultValue = "false") boolean isMobile) {
        return ResponseEntity.ok (biometricService.biometricEnrollment (biometrics, isMobile));
    }
    @GetMapping(BASE_URL_VERSION_ONE + "/patient/{id}")
    public ResponseEntity<CapturedBiometricDTOS> findByPatient(@PathVariable Long id) {
        return ResponseEntity.ok (biometricService.getByPersonId (id));
    }

    @GetMapping(BASE_URL_VERSION_ONE + "/patients/{id}")
    public ResponseEntity<CapturedBiometricDTOS> getByPersonIdCapture(@PathVariable Long id) {
        return ResponseEntity.ok (biometricService.getByPersonIdCapture(id));
    }
    @PostMapping(BASE_URL_VERSION_ONE + "/device")
    public ResponseEntity<BiometricDevice> saveBiometricDevice(@RequestBody BiometricDevice biometricDevice,
                                                               @RequestParam (required = false, defaultValue = "false") boolean active) {
        return ResponseEntity.ok (biometricService.saveBiometricDevice (biometricDevice, active));
    }
    @PutMapping(BASE_URL_VERSION_ONE + "/device/{id}")
    public ResponseEntity<BiometricDevice> update(@PathVariable Long id, @RequestBody BiometricDevice biometricDevice,
                                                  @RequestParam (required = false, defaultValue = "false") boolean active) {
        return ResponseEntity.ok (biometricService.update (id, biometricDevice, active));
    }

    @PutMapping(BASE_URL_VERSION_ONE + "/person/{personId}")
    public ResponseEntity<BiometricDto> updatePersonBiometric(@PathVariable Long personId, @RequestBody BiometricEnrollmentDto biometricEnrollmentDto,
                                                              @RequestParam (required = false, defaultValue = "false") boolean isMobile) {
        return ResponseEntity.ok (biometricService.updatePersonBiometric (personId, biometricEnrollmentDto, isMobile));
    }
    @DeleteMapping(BASE_URL_VERSION_ONE + "/device/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        biometricService.delete (id);
    }
    @GetMapping(BASE_URL_VERSION_ONE + "/devices")
    public ResponseEntity<List<BiometricDevice>> getAllBiometricDevice(@RequestParam (required = false,
            defaultValue = "false") boolean active) {
        return ResponseEntity.ok (biometricService.getAllBiometricDevices(active));
    }

    @GetMapping(BASE_URL_VERSION_ONE + "/person/{personId}")
    public ResponseEntity<List<Biometric>> getAllPersonBiometric(@PathVariable Long personId) {
        return ResponseEntity.ok (biometricService.getAllPersonBiometric(personId));
    }

    @GetMapping(BASE_URL_VERSION_ONE)
    public ResponseEntity<List<Biometric>> getAllPersonBiometric( @RequestParam String personUuid,
                                                                  @RequestParam Integer recapture) {
        return ResponseEntity.ok (biometricService.getBiometricsByPersonUuidAndRecapture(personUuid, recapture));
    }

    @DeleteMapping(BASE_URL_VERSION_ONE + "/person/{personId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllPersonBiometrics(@PathVariable Long personId) {
        biometricService.deleteAllPersonBiometrics (personId);
    }

    @PutMapping(BASE_URL_VERSION_ONE + "/person")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void makeBaseLine(@RequestParam String personUuid, @RequestParam LocalDate captureDate, @RequestParam Integer recapture) {
        biometricService.makeBaseLine (personUuid, captureDate, recapture);
    }
    @DeleteMapping(BASE_URL_VERSION_ONE + "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBiometrics(@PathVariable String id) {
        biometricService.deleteBiometrics (id);
    }

    @GetMapping(BASE_URL_VERSION_ONE + "/grouped/person/{personId}")
    public ResponseEntity<List<GroupedCapturedBiometric>> getGroupedCapturedBiometric(@PathVariable Long personId) {
        return ResponseEntity.ok (biometricService.getGroupedCapturedBiometric(personId));
    }

    @DeleteMapping(BASE_URL_VERSION_ONE)
    public void removeTemplateType(@RequestParam Long personId,
                                   @RequestParam String templateType) {
        biometricService.removeTemplateType(personId, templateType);
    }

    @GetMapping(BASE_URL_VERSION_ONE + "/person/{personUuid}/biometric-count")
    public ResponseEntity<List<GroupedCapturedBiometric>> getPatientBiometricCount(@PathVariable String personUuid){
        try {
            System.out.println("Heeeerereer");
            List<GroupedCapturedBiometric> biometricGroup = biometricService.getPatientBiometricCount(personUuid);
            if (biometricGroup.size() > 0) {
                System.out.println("In here");
                return new ResponseEntity<>(biometricGroup, HttpStatus.OK);
            } else {
                System.out.println("in there out");
                return new ResponseEntity<>(biometricGroup, HttpStatus.NOT_FOUND);
            }
        }catch (Exception e){ e.printStackTrace();}
        return null;
    }
}
