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
import org.lamisplus.modules.biometric.domain.Deduplication;
import org.lamisplus.modules.biometric.domain.dto.*;
import org.lamisplus.modules.biometric.repository.BiometricDeviceRepository;
import org.lamisplus.modules.biometric.repository.BiometricRepository;
import org.lamisplus.modules.biometric.repository.DeduplicationRepository;
import org.lamisplus.modules.patient.domain.entity.Person;
import org.lamisplus.modules.patient.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiometricService {

    private static final LocalDate REPLACE_DATE = LocalDate.now();
    private static final int UN_ARCHIVED = 0;
    private static final int RECAPTURE = 0;
    private static final int ARCHIVED = 1;
    private static final int BIOMETRIC_SIZE = 6;
    private final BiometricRepository biometricRepository;
    private final BiometricDeviceRepository biometricDeviceRepository;
    private final PersonRepository personRepository;
    private  final UserService userService;
    private final DeduplicationRepository deduplicationRepository;

    public BiometricDto biometricEnrollment(BiometricEnrollmentDto biometricEnrollmentDto, Boolean isMobile) {
        AtomicInteger existingCount = new AtomicInteger();
        biometricEnrollmentDto.getCapturedBiometricsList().forEach(b -> {
            if(b.getId()!= null && biometricRepository.findById(b.getId()).isPresent()){
                existingCount.getAndIncrement();
            }
        });

        if(existingCount.get() > 0){
            LOG.info("Patient: " + biometricEnrollmentDto.getPatientId() + "'s prints have already been synced.");
            return BiometricDto.builder()
                    .numberOfFingers(biometricEnrollmentDto.getCapturedBiometricsList().size())
                    .personId(biometricEnrollmentDto.getPatientId())
                    .date(biometricEnrollmentDto.getEnrollmentDate() != null ? biometricEnrollmentDto.getEnrollmentDate() : LocalDate.now())
                    .iso(true).build();
        }

        if(biometricEnrollmentDto.getCapturedBiometricsList().size() < BIOMETRIC_SIZE){
            LOG.error("Biometric Template is less than 6");
            throw new IllegalTypeException(BiometricEnrollmentDto.class,"Biometric Error:", "Biometric template is less than 6");
        }

        if(biometricEnrollmentDto.getType().equals(BiometricEnrollmentDto.Type.ERROR)){
            LOG.error("The templates are not valid");
            throw new IllegalTypeException(BiometricEnrollmentDto.class,"Biometric Error:", "Type is Error");
        }

        Long personId = biometricEnrollmentDto.getPatientId ();
        Person person = personRepository.findById (personId)
                .orElseThrow(() -> new EntityNotFoundException(BiometricEnrollmentDto.class,"patientId:", ""+personId));
    
        if(biometricRepository.getBiometricByDate(person.getUuid(), LocalDate.now()) > 0){
            LOG.error("Fingerprints for today already synced for client: " + personId);
            throw new IllegalTypeException(BiometricEnrollmentDto.class,"Biometric Error:", "Cannot capture on same date");
        }
        Optional<Integer> opRecapture = biometricRepository.findMaxRecapture(person.getUuid());
        Optional<String> opNullpRecapture = biometricRepository.findNotNullReplaceDate(person.getUuid());
        Integer recapture=-1;
        if(opRecapture.isPresent())recapture=Integer.valueOf(opRecapture.get());
//            if (opNullpRecapture.isPresent()) {
//                recapture = +2;
//        } else Integer recap = ++recapture;

        if (opNullpRecapture.isPresent()) {
            recapture += 2;
        } else {
            recapture++;
        }
        Integer recap = recapture;

        String biometricType = biometricEnrollmentDto.getBiometricType ();
        LocalDate enrollmentDate = (isMobile && biometricEnrollmentDto.getEnrollmentDate() != null)? biometricEnrollmentDto.getEnrollmentDate() : LocalDate.now();
        String deviceName = biometricEnrollmentDto.getDeviceName ();
        String reason = biometricEnrollmentDto.getReason();

        List<CapturedBiometricDto> capturedBiometricsList = biometricEnrollmentDto.getCapturedBiometricsList ();
        List<Biometric> biometrics = capturedBiometricsList.stream ()
                .map (capturedBiometricDto -> convertDtoToEntity (capturedBiometricDto, person, biometricType, deviceName,
                        reason, capturedBiometricDto.getImageQuality(),
                        recap, biometricEnrollmentDto.getRecaptureMessage(), capturedBiometricsList.size(), enrollmentDate, isMobile, biometricEnrollmentDto.getMatchType(),
                        biometricEnrollmentDto.getMatchPersonUuid(), biometricEnrollmentDto.getMatchBiometricId()))
                .collect (Collectors.toList ());
        biometricRepository.saveAll (biometrics);

        if(biometricEnrollmentDto.getDeduplication() != null){
            LOG.info("Deduplication Data ***** {}", biometricEnrollmentDto.getDeduplication());
            Deduplication deduplication = new Deduplication();

            deduplication.setPersonUuid(person.getUuid());
            deduplication.setDetails(biometricEnrollmentDto.getDeduplication().getDetails());
            deduplication.setImperfectMatchCount(biometricEnrollmentDto.getDeduplication().getImperfectMatchCount());
            deduplication.setPerfectMatchCount(biometricEnrollmentDto.getDeduplication().getPerfectMatchCount());
            deduplication.setBaselineFingerCount(biometricEnrollmentDto.getDeduplication().getBaselineFingerCount());
            deduplication.setRecaptureFingerCount(biometricEnrollmentDto.getDeduplication().getRecaptureFingerCount());
            deduplication.setUnmatchedCount(biometricEnrollmentDto.getDeduplication().getUnmatchedCount());
            deduplication.setMatchedCount(biometricEnrollmentDto.getDeduplication().getMatchedCount());
            deduplication.setDeduplicationDate(enrollmentDate);

            // Saving recapture fingerprints deduplication information
            deduplicationRepository.save(deduplication);
        }
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
        capturedBiometricDto.setMatchType(biometric.getMatchType());
        capturedBiometricDto.setMatchPersonUuid(biometric.getPersonUuid());
        capturedBiometricDto.setMatchBiometricId(biometric.getId());
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
                        capturedBiometricDto.setMatchType(biometric1.getMatchType());
                        capturedBiometricDto.setMatchPersonUuid(biometric1.getPersonUuid());
                        capturedBiometricDto.setMatchBiometricId(biometric1.getMatchBiometricId());
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
           // BiometricEnrollmentDto biometricEnrollmentDto,
            Person person, String biometricType,
            String deviceName, String reason, int imageQuality,
            Integer recapture, String recaptureMessage, Integer count, LocalDate date, Boolean isMobile, String matchType, String matchPerson, String matchBiometric) {
        Biometric biometric = new Biometric ();
//        check for mobile Id exist
        if (capturedBiometricDto.getId() != null && isMobile) {
            biometric.setId (capturedBiometricDto.getId());
        }else{
            biometric.setId (UUID.randomUUID ().toString ());
        }

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
        biometric.setMatchType(capturedBiometricDto.getMatchType());
        biometric.setMatchPersonUuid(person.getUuid ());
        biometric.setMatchBiometricId(capturedBiometricDto.getId());
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

            //update biometric recapture column to base line
            biometricRepository.updateRecaptureNullField();
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

    public void makeBaseLine(String personUuid, LocalDate captureDate, Integer recapture) {
        List<Biometric> recapturedBiometrics = biometricRepository.findAllByPersonUuidAndDateAndArchived(personUuid, captureDate, UN_ARCHIVED);
        List<Biometric> baselineBiometrics = biometricRepository.findAllByPersonUuidAndRecaptureAndArchived(personUuid, RECAPTURE, UN_ARCHIVED);

        // Filter recapturedBiometrics based on specific conditions
        List<Biometric> filteredRecapturedBiometrics = recapturedBiometrics.stream()
                .filter(recap -> recap.getPersonUuid().equals(personUuid))
                .filter(recap -> recap.getRecapture().equals(recapture))
                .filter(recap -> recap.getDate().equals(captureDate))
                .collect(Collectors.toList());

        if (!filteredRecapturedBiometrics.isEmpty()) {

            // Override recapture and replaceDate properties for filteredRecapturedBiometrics
            filteredRecapturedBiometrics = filteredRecapturedBiometrics.stream()
                    .map(biometric -> {
                        biometric.setRecapture(RECAPTURE);
                        biometric.setReplaceDate(REPLACE_DATE);
                        return biometric;
                    })
                    .collect(Collectors.toList());

            // Increment recapture if it's not 0
//            recapturedBiometrics = recapturedBiometrics.stream()
//                    .map(biometric -> {
//                        if (biometric.getRecapture() != 0) {
//                            biometric.setRecapture(biometric.getRecapture() + 1);
//                        }
//                        return biometric;})
//                    .collect(Collectors.toList());
        }else {
            throw new EntityNotFoundException(Biometric.class, "Recapture", "biometrics");
        }

        if(!baselineBiometrics.isEmpty()){
            baselineBiometrics = baselineBiometrics.stream()
                    .map(biometric -> {biometric.setArchived(ARCHIVED); return biometric;})
                    .collect(Collectors.toList());
        }else {
            throw new EntityNotFoundException(Biometric.class, "Baseline", "biometrics");
        }
        List<Biometric> merged = new ArrayList<>();
        merged.addAll(recapturedBiometrics);
        merged.addAll(baselineBiometrics);
        biometricRepository.saveAll(merged);
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
        return biometricRepository.findAllByPersonUuidAndRecaptureAndArchived(personUuid, recapture, 0);
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

    public List<GroupedCapturedBiometric> getPatientBiometricCount(String personUuid) {
        try {
            System.out.println("hereereer");
            return biometricRepository.getPatientBiometricCount(personUuid);
        }catch (Exception e){
            System.out.println("Excpeee");
            e.printStackTrace();}
        return null;
    }
}
