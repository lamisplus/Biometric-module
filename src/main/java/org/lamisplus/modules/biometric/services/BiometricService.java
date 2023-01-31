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
    private List<BiometricDevice> saveDevices(BiometricDevice biometricDevice, Boolean active){
        List <BiometricDevice> biometricDevices = new ArrayList<>();

        if(active){
            Optional<BiometricDevice> optional = biometricDeviceRepository.findByActive(true);
            if(optional.isPresent()) {
                BiometricDevice biometricDevice1 = optional.get();
                biometricDevice1.setActive(false);
                biometricDevices.add(biometricDevice1);
            }
            biometricDevice.setActive(true);
        }else {
            biometricDevice.setActive(false);
        }
        biometricDevices.add(biometricDevice);
        return biometricDevices;
    }

    public BiometricDevice saveBiometricDevice(BiometricDevice biometricDevice, Boolean active){
        List <BiometricDevice> biometricDevices = this.saveDevices(biometricDevice,active);
        biometricDeviceRepository.saveAll(biometricDevices);
        return biometricDevice;
    }

    public BiometricDevice update(Long id, BiometricDevice updatedBiometricDevice, Boolean active){
       biometricDeviceRepository
                .findById(id)
                .orElseThrow(()-> new EntityNotFoundException(BiometricDevice.class, "id", ""+id));


        updatedBiometricDevice.setId(id);
        List <BiometricDevice> biometricDevices = this.saveDevices(updatedBiometricDevice,active);
        biometricDeviceRepository.saveAll(biometricDevices);

        return updatedBiometricDevice;
    }
    public void delete(Long id) {
        BiometricDevice biometricDevice = biometricDeviceRepository
                .findById(id)
                .orElseThrow(()-> new EntityNotFoundException(BiometricDevice.class, "id", ""+id));
        biometricDeviceRepository.delete(biometricDevice);
    }

    public List<BiometricDevice> getAllBiometricDevices(boolean active){
        if(active){
            return biometricDeviceRepository.getAllByActiveIsTrue();
        }
        return biometricDeviceRepository.findAll();
    }

    public BiometricDto updatePersonBiometric(Long personId, BiometricEnrollmentDto biometricEnrollmentDto) {
        biometricRepository.deleteAll(this.getPersonBiometrics(personId));
        return biometricEnrollment(biometricEnrollmentDto);
    }
    public List<Biometric> getAllPersonBiometric(Long personId) {
        List<Biometric> personBiometrics = this.getPersonBiometrics(personId);
        if(personBiometrics.isEmpty())throw new EntityNotFoundException(Biometric.class,"patientId:", ""+personId);
        return personBiometrics;
    }
    public List<Biometric> getPersonBiometrics(Long personId){
        Person person = personRepository.findById (personId)
                .orElseThrow(() -> new EntityNotFoundException(Person.class,"patientId:", ""+personId));
        List<Biometric> personBiometrics = biometricRepository.findAllByPersonUuid(person.getUuid());
        return personBiometrics;
    }
    public void deleteAllPersonBiometrics(Long personId) {
        this.biometricRepository.deleteAll(this.getAllPersonBiometric(personId));
    }
    public void deleteBiometrics(String id) {
        Biometric biometric = biometricRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException(Biometric.class,"id:", ""+id));
        biometricRepository.deleteById(biometric.getId());
    }
}
