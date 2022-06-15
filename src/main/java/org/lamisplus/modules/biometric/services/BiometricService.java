package org.lamisplus.modules.biometric.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.lamisplus.modules.base.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.base.controller.apierror.IllegalTypeException;
import org.lamisplus.modules.base.domain.entities.User;
import org.lamisplus.modules.base.service.UserService;
import org.lamisplus.modules.biometric.domain.Biometric;
import org.lamisplus.modules.biometric.domain.BiometricDevice;
import org.lamisplus.modules.biometric.domain.dto.*;
import org.lamisplus.modules.biometric.repository.BiometricDeviceRepository;
import org.lamisplus.modules.biometric.repository.BiometricRepository;
import org.lamisplus.modules.patient.domain.entity.Person;
import org.lamisplus.modules.patient.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiometricService {
    private final BiometricRepository biometricRepository;
    private final BiometricDeviceRepository biometricDeviceRepository;
    private final PersonRepository personRepository;
    private  final UserService userService;

    public BiometricDto biometricEnrollment(BiometricEnrollmentDto biometricEnrollmentDto) {
        if(biometricEnrollmentDto.getType().equals(BiometricEnrollmentDto.Type.ERROR)){
            //IllegalTypeException
            throw new IllegalTypeException(BiometricEnrollmentDto.class,"Biometric Error:", "Type is Error");
        }
        Long personId = biometricEnrollmentDto.getPatientId ();
        Person person = personRepository.findById (personId)
                .orElseThrow(() -> new EntityNotFoundException(BiometricEnrollmentDto.class,"patientId:", ""+personId));

        String biometricType = biometricEnrollmentDto.getBiometricType ();
        String deviceName = biometricEnrollmentDto.getDeviceName ();
        List<CapturedBiometricDto> capturedBiometricsList = biometricEnrollmentDto.getCapturedBiometricsList ();
        List<Biometric> biometrics = capturedBiometricsList.stream ()
                .map (capturedBiometricDto -> convertDtoToEntity (capturedBiometricDto, person, biometricType, deviceName))
                .collect (Collectors.toList ());
        biometricRepository.saveAll (biometrics);
        return getBiometricDto (biometrics, personId);
    }
    public CapturedBiometricDTOS getByPersonId(Long personId) {
        Person person = personRepository.findById (personId)
                .orElseThrow (()-> new EntityNotFoundException (Person.class, "Id", ""+personId));
        List<Biometric> biometrics = biometricRepository.findAllByPersonUuid (person.getUuid ());
        final CapturedBiometricDTOS[] capturedBiometricDTOS = {new CapturedBiometricDTOS()};

        if(biometrics.isEmpty()) throw new EntityNotFoundException(Biometric.class, "personId", "" +personId);
        biometrics.forEach(biometric -> capturedBiometricDTOS[0] = getCapturedBiometricDTOS(capturedBiometricDTOS[0],
                personId, biometric, biometrics));
        return capturedBiometricDTOS[0];
    }
    private CapturedBiometricDTOS getCapturedBiometricDTOS(CapturedBiometricDTOS capturedBiometricDtos, Long personId,
                                                           Biometric biometric, List<Biometric> biometrics){
        if(capturedBiometricDtos.getPersonId() == null) {
            capturedBiometricDtos.setPersonId(personId);
            capturedBiometricDtos.setNumberOfFingers(biometrics.size());
            capturedBiometricDtos.setDate(biometric.getDate());
        }
        CapturedBiometricDto capturedBiometricDto = new CapturedBiometricDto();
        capturedBiometricDto.setTemplate(biometric.getTemplate());
        capturedBiometricDto.setTemplateType(biometric.getTemplateType());
        capturedBiometricDtos.getCapturedBiometricsList().add(capturedBiometricDto);

        return capturedBiometricDtos;
    }
    private BiometricDto getBiometricDto(List<Biometric> biometricList, Long personId) {
        return BiometricDto.builder ()
                .numberOfFingers (biometricList.size ())
                .personId (personId)
                .date (getDate (biometricList))
                .iso (true).build ();
    }
    @Nullable
    private LocalDate getDate(List<Biometric> biometricList) {
        if (! biometricList.isEmpty ()) {
            return biometricList.get (0).getDate ();
        }
        return null;
    }
    private Biometric convertDtoToEntity(
            CapturedBiometricDto capturedBiometricDto,
            Person person, String biometricType,
            String deviceName) {
        Biometric biometric = new Biometric ();
        biometric.setId (UUID.randomUUID ().toString ());
        biometric.setBiometricType (biometricType);
        biometric.setDeviceName (deviceName);
        biometric.setTemplate (capturedBiometricDto.getTemplate ());
        biometric.setTemplateType (capturedBiometricDto.getTemplateType ());
        biometric.setDate (LocalDate.now ());
        biometric.setIso (true);
        biometric.setPersonUuid (person.getUuid ());
        Optional<User> userWithRoles = userService.getUserWithRoles ();
        if(userWithRoles.isPresent ()){
            User user = userWithRoles.get ();
            biometric.setFacilityId (user.getCurrentOrganisationUnitId ());
        }
        return biometric;
    }
    public BiometricDevice update(Long id, BiometricDevice biometricDevice){
        biometricDeviceRepository
                .findById(id)
                .orElseThrow(()-> new EntityNotFoundException(BiometricDevice.class, "id", ""+id));
        biometricDevice.setId(id);
        return biometricDeviceRepository.save(biometricDevice);
    }
    public void delete(Long id) {
        BiometricDevice biometricDevice = biometricDeviceRepository
                .findById(id)
                .orElseThrow(()-> new EntityNotFoundException(BiometricDevice.class, "id", ""+id));
        biometricDeviceRepository.delete(biometricDevice);

    }
}
