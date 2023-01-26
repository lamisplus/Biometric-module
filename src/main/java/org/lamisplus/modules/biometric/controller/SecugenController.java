package org.lamisplus.modules.biometric.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.biometric.domain.dto.BiometricEnrollmentDto;
import org.lamisplus.modules.biometric.domain.dto.CaptureRequestDTO;
import org.lamisplus.modules.biometric.domain.dto.DeviceDTO;
import org.lamisplus.modules.biometric.domain.dto.ErrorCodeDTO;
import org.lamisplus.modules.biometric.services.SecugenManager;
import org.lamisplus.modules.biometric.services.SecugenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SecugenController {
    private final SecugenService secugenService;
    //Versioning through URI Path
    private final String SECUGEN_URL_VERSION_ONE = "/api/v1/biometrics/secugen";
    private final String BIOMETRICS_URL_VERSION_ONE = "/api/v1/biometrics";
    private final SecugenManager secugenManager;


    @GetMapping(SECUGEN_URL_VERSION_ONE + "/server")
    public String getServerUrl() {
        return secugenManager.getSecugenProperties().getServerUrl();
    }

    @GetMapping(SECUGEN_URL_VERSION_ONE + "/reader")
    public ResponseEntity<Object> getReaders() {
        List<DeviceDTO> devices = secugenManager.getDevices();
        return ResponseEntity.ok(devices);
    }

    @PostMapping(BIOMETRICS_URL_VERSION_ONE + "/enrollment")
    public BiometricEnrollmentDto enrollment(@RequestParam String reader,
                                             @RequestParam(required = false, defaultValue = "false") Boolean isNew,
                                             @Valid @RequestBody CaptureRequestDTO captureRequestDTO) {
        return secugenService.enrollment(reader, isNew, captureRequestDTO);
    }


    /*@PostMapping(BASE_URL_VERSION_ONE + "/enrollment2")
    public BiometricEnrollmentDto enrollment2(@RequestParam String reader,
                                              @Valid @RequestBody CaptureRequestDTO captureRequestDTO) {
        BiometricEnrollmentDto biometric = secugenService.getBiometricEnrollmentDto(captureRequestDTO);
        biometric.setMessage(new HashMap<String, String>());
        if(!reader.equals("SG_DEV_AUTO")) {
            biometric.getMessage().put("ERROR", "READER NOT AVAILABLE");
            biometric.setType(BiometricEnrollmentDto.Type.ERROR);
            return biometric;
        }
        if(!biometric.getBiometricType().equals("FINGERPRINT")) {
            biometric.getMessage().put("ERROR", "TemplateType not FINGERPRINT");
            biometric.setType(BiometricEnrollmentDto.Type.ERROR);
            return biometric;
        }

        try(InputStream in=new ClassPathResource("biometrics_payload.txt").getInputStream()){
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            BiometricEnrollmentDto biometric1 = mapper.readValue(in, BiometricEnrollmentDto.class);
            return biometric1;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }*/

    @GetMapping(SECUGEN_URL_VERSION_ONE + "/boot")
    public ErrorCodeDTO boot(@RequestParam String reader) {
        return secugenService.boot(reader);
    }
}
