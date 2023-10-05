package org.lamisplus.modules.biometric.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    public BiometricDto biometricEnrollment(BiometricEnrollmentDto biometricEnrollmentDto, Boolean isMobile) {
        if(biometricEnrollmentDto.getType().equals(BiometricEnrollmentDto.Type.ERROR)){
            //IllegalTypeException
            throw new IllegalTypeException(BiometricEnrollmentDto.class,"Biometric Error:", "Type is Error");
        }
        Long personId = biometricEnrollmentDto.getPatientId ();
        Person person = personRepository.findById (personId)
                .orElseThrow(() -> new EntityNotFoundException(BiometricEnrollmentDto.class,"patientId:", ""+personId));

        Optional<Integer> opRecapture = biometricRepository.findMaxRecapture(person.getUuid());
        Integer recapture=-1;
        if(opRecapture.isPresent())recapture=Integer.valueOf(opRecapture.get());
        Integer recap = ++recapture;
        String biometricType = biometricEnrollmentDto.getBiometricType ();
        LocalDate enrollmentDate = (isMobile && biometricEnrollmentDto.getEnrollmentDate() != null)? biometricEnrollmentDto.getEnrollmentDate() : LocalDate.now();
        String deviceName = biometricEnrollmentDto.getDeviceName ();
        String reason = biometricEnrollmentDto.getReason();
        List<CapturedBiometricDto> capturedBiometricsList = biometricEnrollmentDto.getCapturedBiometricsList ();
        List<Biometric> biometrics = capturedBiometricsList.stream ()
                .map (capturedBiometricDto -> convertDtoToEntity (capturedBiometricDto, person, biometricType, deviceName,
                        reason, capturedBiometricDto.getImageQuality(),
                        recap, biometricEnrollmentDto.getRecaptureMessage(), capturedBiometricsList.size(), enrollmentDate, isMobile))
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

    public CapturedBiometricDTOS getByPersonIdCapture(Long personId) {
        Person person = personRepository.findById (personId)
                .orElseThrow (()-> new EntityNotFoundException (Person.class, "Id", ""+personId));
        List<String> recaptures = biometricRepository.findRecapturesByPersonUuidAndRecaptures(person.getUuid ());
        return getCapturedBiometrics(recaptures, person.getUuid());
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
        if(biometric.getHashed() != null)capturedBiometricDto.setHashed(biometric.getHashed());
        capturedBiometricDto.setImageQuality(biometric.getImageQuality());
        capturedBiometricDtos.getCapturedBiometricsList().add(capturedBiometricDto);

        return capturedBiometricDtos;
    }

    private CapturedBiometricDTOS getCapturedBiometrics(List<String> recaptures,
                                                           String personUuid){
        CapturedBiometricDto capturedBiometricDto = new CapturedBiometricDto();
        CapturedBiometricDTOS capturedBiometricDtos = new CapturedBiometricDTOS();
        List<List<CapturedBiometricDto>> capturedBiometricsList = new ArrayList<>();
        recaptures.forEach(recapture->{
            List<CapturedBiometricDto> capturedBiometrics = new ArrayList<>();
            biometricRepository
                    .findAllByPersonUuidAndRecapture(personUuid, recapture)
                    .forEach(biometric1 -> {
                        capturedBiometricDto.setTemplate(biometric1.getTemplate());
                        capturedBiometricDto.setTemplateType(biometric1.getTemplateType());
                        capturedBiometricDto.setImageQuality(biometric1.getImageQuality());
                        if(biometric1.getHashed() != null)capturedBiometricDto.setHashed(biometric1.getHashed());
                        capturedBiometrics.add(capturedBiometricDto);
                    });
            capturedBiometricsList.add(capturedBiometrics);
        });
        capturedBiometricDtos.setCapturedBiometricsList2(capturedBiometricsList);
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
            String deviceName, String reason, int imageQuality,
            Integer recapture, String recaptureMessage, Integer count, LocalDate date, Boolean isMobile) {
        Biometric biometric = new Biometric ();
        biometric.setId (UUID.randomUUID ().toString ());
        biometric.setBiometricType (biometricType);
        biometric.setDeviceName (deviceName);
        biometric.setTemplate (capturedBiometricDto.getTemplate ());
        biometric.setTemplateType (capturedBiometricDto.getTemplateType ());
        if(capturedBiometricDto.getHashed() != null)biometric.setHashed(capturedBiometricDto.getHashed());
        biometric.setDate (date);
        biometric.setIso (true);
        biometric.setReason(reason);
        biometric.setVersionIso20(true);
        biometric.setPersonUuid (person.getUuid ());
        biometric.setImageQuality(imageQuality);
        biometric.setRecapture(recapture);
        biometric.setRecaptureMessage(recaptureMessage);
        biometric.setCount(count);
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

    public BiometricDto updatePersonBiometric(Long personId, BiometricEnrollmentDto biometricEnrollmentDto, Boolean isMobile) {
        biometricRepository.deleteAll(this.getPersonBiometrics(personId));
        return biometricEnrollment(biometricEnrollmentDto, isMobile);
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

    /**
     * Get person biometric a list of groups.
     * @param personId
     * @return a List of GroupedCapturedBiometric
     */
    public List<GroupedCapturedBiometric> getGroupedCapturedBiometric(Long personId){
        List<GroupedCapturedBiometric> groupedCapturedBiometrics = biometricRepository.getGroupedPersonBiometric(personId);
        LOG.info("Size is {}", groupedCapturedBiometrics.size());
        return groupedCapturedBiometrics;
    }

    /**
     * Get person biometric by person uuid and recapture.
     * @param personUuid
     * @param recapture
     * @return a List of a person Biometric for a specific captured instance
     */
    public List<Biometric> getBiometricsByPersonUuidAndRecapture(String personUuid, Integer recapture) {
        List<Biometric> personBiometrics = biometricRepository.findAllByPersonUuidAndRecapture(personUuid, recapture);
        return personBiometrics;
    }

    /**
     * Removes a specific template type (finger) from the list of capture biometric.
     * @param personId
     * @param templateType
     * @return nothing (void)
     */
    public void removeTemplateType(Long personId, String templateType){
        //Checking if the person exist in the list
        if(!BiometricStoreDTO.getPatientBiometricStore().isEmpty() && BiometricStoreDTO.getPatientBiometricStore().get(personId) != null){

            //removes the specific finger or template type
            final List<CapturedBiometricDto> capturedBiometricsListDTO = BiometricStoreDTO
                    .getPatientBiometricStore()
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList())
                    .stream()
                    .filter(c->!c.getTemplateType().equals(templateType))
                    .collect(Collectors.toList());

            //removes the person
            BiometricStoreDTO.getPatientBiometricStore().remove(personId);
            //fills the list with the specific finger or template type removed
            BiometricStoreDTO.getPatientBiometricStore().put(personId, capturedBiometricsListDTO);
        }
    }

}
