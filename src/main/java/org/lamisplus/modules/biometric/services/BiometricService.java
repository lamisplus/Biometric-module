package org.lamisplus.modules.biometric.services;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lamisplus.modules.base.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.base.domain.entities.User;
import org.lamisplus.modules.base.service.UserService;
import org.lamisplus.modules.biometric.domain.Biometric;
import org.lamisplus.modules.biometric.domain.dto.BiometricDto;
import org.lamisplus.modules.biometric.domain.dto.BiometricEnrollmentDto;
import org.lamisplus.modules.biometric.domain.dto.CapturedBiometricDto;
import org.lamisplus.modules.biometric.repository.BiometricRepository;
import org.lamisplus.modules.patient.domain.entity.Person;
import org.lamisplus.modules.patient.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BiometricService {
    private final BiometricRepository biometricRepository;
    private final PersonRepository personRepository;

    private  final UserService userService;


    public BiometricDto biometricEnrollment(BiometricEnrollmentDto biometricEnrollmentDto) {
        Long personId = biometricEnrollmentDto.getPatientId ();
        Person person = personRepository.findById (personId).orElseThrow (getEntityNotFoundExceptionSupplier (personId));
        String biometricType = biometricEnrollmentDto.getBiometricType ();
        String deviceName = biometricEnrollmentDto.getDeviceName ();
        List<CapturedBiometricDto> capturedBiometricsList = biometricEnrollmentDto.getCapturedBiometricsList ();
        List<Biometric> biometrics = capturedBiometricsList.stream ()
                .map (capturedBiometricDto -> convertDtoToEntity (capturedBiometricDto, person, biometricType, deviceName))
                .collect (Collectors.toList ());
        biometricRepository.saveAll (biometrics);
        return getBiometricDto (biometrics, personId);
    }

    public List<Biometric> getByPersonId(Long personId) {
        Person person = personRepository.findById (personId).orElseThrow (getEntityNotFoundExceptionSupplier (personId));
        return biometricRepository.findAllByPersonUuid (person.getUuid ());
    }


    @NotNull
    private Supplier<EntityNotFoundException> getEntityNotFoundExceptionSupplier(Long personId) {
        return () -> new EntityNotFoundException (BiometricService.class, "Person not found with given Id " + personId);
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
}
