package org.lamisplus.modules.biometric.repository;

import org.lamisplus.modules.biometric.domain.Biometric;
import org.lamisplus.modules.biometric.domain.ClientIdentificationProject;
import org.lamisplus.modules.biometric.domain.dto.BiometricPerson;
import org.lamisplus.modules.biometric.domain.dto.ClientIdentificationDTO;
import org.lamisplus.modules.biometric.domain.dto.GroupedCapturedBiometric;
import org.lamisplus.modules.biometric.domain.dto.StoredBiometric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BiometricRepository extends JpaRepository<Biometric, String> {
    List<Biometric> findAllByPersonUuid(String personUuid);

    @Query(value ="SELECT DISTINCT ON (person_uuid) person_uuid, replace_date FROM biometric WHERE replace_date IS NOT NULL AND person_uuid=?1 AND archived = 0", nativeQuery = true)
    Optional<String> findNotNullReplaceDate (String personUuid);
    @Query(value ="SELECT DISTINCT recapture FROM biometric WHERE person_uuid=?1", nativeQuery = true)
    List<String> findRecapturesByPersonUuidAndRecaptures(String personUuid);

    @Query(value ="SELECT MAX(recapture) FROM biometric WHERE person_uuid=?1 AND archived = 0", nativeQuery = true)
    Optional<Integer> findMaxRecapture(String personUuid);
    
    @Query(value ="SELECT COUNT(person_uuid) FROM biometric WHERE person_uuid=?1 AND enrollment_date=?2 AND archived=0", nativeQuery = true)
    Integer getBiometricByDate(String personUuid, LocalDate enrollmentDate);
    
    List<Biometric> findAllByPersonUuidAndRecapture(String personUuid, String recapture);
    @Query(value ="SELECT * FROM biometric WHERE last_modified_date > ?1 AND facility_id=?2", nativeQuery = true)
    public List<Biometric> getAllDueForServerUpload(LocalDateTime dateLastSync, Long facilityId);

    List<Biometric> findAllByFacilityId(Long facilityId);

    @Query(value="SELECT person_uuid, id, (CASE template_type WHEN 'Right Middle Finger' THEN template END) AS rightMiddleFinger,  \n" +
            "    (CASE template_type WHEN 'Right Thumb' THEN template END) AS rightThumb, \n" +
            "\t(CASE template_type WHEN 'Right Index Finger' THEN template END) AS rightIndexFinger, \n" +
            "\t(CASE template_type WHEN 'Right Ring Finger' THEN template END) AS rightRingFinger,\n" +
            "\t(CASE template_type WHEN 'Right Little Finger' THEN template END) AS rightLittleFinger,\n" +
            "\t(CASE template_type WHEN 'Left Index Finger' THEN template END) AS leftIndexFinger,  \n" +
            "    (CASE template_type WHEN 'Left Middle Finger' THEN template END) AS leftMiddleFinger, \n" +
            "\t(CASE template_type WHEN 'Left Thumb' THEN template END) AS leftThumb,\n" +
            "\t(CASE template_type WHEN 'Left Ring Finger' THEN template END) AS leftRingFinger,\n" +
            "\t(CASE template_type WHEN 'Left Little Finger' THEN template END) AS leftLittleFinger\t\n" +
            "\tFrom biometric WHERE facility_id=?1 AND ENCODE(CAST(template AS BYTEA), 'hex') LIKE ?2 AND archived=0 Group By person_uuid, id", nativeQuery = true)
    Set<StoredBiometric> findByFacilityIdWithTemplate();


    @Query(value="SELECT person_uuid AS personUuid, recapture, string_agg((CASE template_type WHEN 'Right Middle Finger' THEN template END), '') AS rightMiddleFinger,   \n" +
            "                string_agg((CASE template_type WHEN 'Right Thumb' THEN template END), '') AS rightThumb,  \n" +
            "            string_agg((CASE template_type WHEN 'Right Index Finger' THEN template END), '') AS rightIndexFinger,  \n" +
            "            string_agg((CASE template_type WHEN 'Right Ring Finger' THEN template END), '') AS rightRingFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Right Little Finger' THEN template END), '') AS rightLittleFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Left Index Finger' THEN template END), '') AS leftIndexFinger,   \n" +
            "            string_agg((CASE template_type WHEN 'Left Middle Finger' THEN template END), '') AS leftMiddleFinger,  \n" +
            "            string_agg((CASE template_type WHEN 'Left Thumb' THEN template END), '') AS leftThumb, \n" +
            "            string_agg((CASE template_type WHEN 'Left Ring Finger' THEN template END), '') AS leftRingFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Left Little Finger' THEN template END), '') AS leftLittleFinger \n" +
            "            From biometric WHERE facility_id=?1 AND ENCODE(CAST(template AS BYTEA), 'hex') LIKE ?2 AND archived=0" +
            " GROUP BY person_uuid, recapture", nativeQuery = true)
    List<StoredBiometric> findByFacilityIdWithTemplate(Long facilityId, String template);

    @Query(value="SELECT person_uuid AS personUuid, recapture, string_agg((CASE template_type WHEN 'Right Middle Finger' THEN template END), '') AS rightMiddleFinger,   \n" +
            "                string_agg((CASE template_type WHEN 'Right Thumb' THEN template END), '') AS rightThumb,  \n" +
            "            string_agg((CASE template_type WHEN 'Right Index Finger' THEN template END), '') AS rightIndexFinger,  \n" +
            "            string_agg((CASE template_type WHEN 'Right Ring Finger' THEN template END), '') AS rightRingFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Right Little Finger' THEN template END), '') AS rightLittleFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Left Index Finger' THEN template END), '') AS leftIndexFinger,   \n" +
            "            string_agg((CASE template_type WHEN 'Left Middle Finger' THEN template END), '') AS leftMiddleFinger,  \n" +
            "            string_agg((CASE template_type WHEN 'Left Thumb' THEN template END), '') AS leftThumb, \n" +
            "            string_agg((CASE template_type WHEN 'Left Ring Finger' THEN template END), '') AS leftRingFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Left Little Finger' THEN template END), '') AS leftLittleFinger \n" +
            "            From biometric WHERE facility_id=?1 AND ENCODE(CAST(template AS BYTEA), 'hex') LIKE ?2 AND archived=0" +
            " GROUP BY person_uuid, recapture", nativeQuery = true)
    Page<StoredBiometric> findByFacilityIdWithTemplate(Long facilityId, String template, Pageable pageable);


    @Query(value="SELECT uuid FROM patient_person WHERE id=?1", nativeQuery = true)
    Optional<String> getPersonUuid(Long patientId);

    @Query(value="SELECT template FROM biometric WHERE person_uuid=?1 AND template_type=?2 AND recapture=?3", nativeQuery = true)
    Optional<byte[]> getPersonUuidTemplateRecapture(String personUuid, String templateType, Integer recapture);

    @Query(value="SELECT person_uuid AS personUuid, recapture, string_agg((CASE template_type WHEN 'Right Middle Finger' THEN template END), '') AS rightMiddleFinger,   \n" +
            "                string_agg((CASE template_type WHEN 'Right Thumb' THEN template END), '') AS rightThumb,  \n" +
            "            string_agg((CASE template_type WHEN 'Right Index Finger' THEN template END), '') AS rightIndexFinger,  \n" +
            "            string_agg((CASE template_type WHEN 'Right Ring Finger' THEN template END), '') AS rightRingFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Right Little Finger' THEN template END), '') AS rightLittleFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Left Index Finger' THEN template END), '') AS leftIndexFinger,   \n" +
            "            string_agg((CASE template_type WHEN 'Left Middle Finger' THEN template END), '') AS leftMiddleFinger,  \n" +
            "            string_agg((CASE template_type WHEN 'Left Thumb' THEN template END), '') AS leftThumb, \n" +
            "            string_agg((CASE template_type WHEN 'Left Ring Finger' THEN template END), '') AS leftRingFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Left Little Finger' THEN template END), '') AS leftLittleFinger \n" +
            "            From biometric WHERE facility_id=?1 AND person_uuid=?2 AND recapture=?3 " +
            "AND ENCODE(CAST(template AS BYTEA), 'hex') LIKE ?4 and archived=0" +
            " GROUP BY person_uuid, recapture", nativeQuery = true)
    List<StoredBiometric> findByFacilityIdWithTemplateAndPersonUuid(Long facilityId, String personUuid, Integer recapture, String template);




    @Query(value="SELECT template FROM biometric WHERE person_uuid=?1 AND recapture=?2", nativeQuery = true)
    List<byte[]> getPersonUuidTemplatesForRecapture(String personUuid, Integer recapture);

    @Query(value="SELECT DISTINCT (b.recapture) AS recapture, " +
            "b.enrollment_date AS captureDate, b.person_uuid AS personUuid, " +
            "b.count, b.archived, replace_date AS replaceDate " +
            "FROM biometric b " +
            "INNER JOIN patient_person pp ON pp.uuid=b.person_uuid " +
            "WHERE pp.id=?1 AND b.archived != 1 AND pp.archived=0 " +
            "ORDER BY recapture DESC", nativeQuery = true)
    List<GroupedCapturedBiometric> getGroupedPersonBiometric (Long patientId);
    
    List<Biometric> findAllByPersonUuidAndRecapture(String personUuid, Integer recapture);

    List<Biometric> findAllByPersonUuidAndRecaptureAndArchived(String personUuid, Integer recapture, Integer archive);
    List<Biometric> findAllByPersonUuidAndDateAndArchived(String personUuid, LocalDate captureDate, Integer archive);


    @Query(value="SELECT id, first_name AS firstName, surname FROM patient_person WHERE person_uuid=?1", nativeQuery = true)
    Optional<BiometricPerson> getBiometricPerson(String personUuid);

    @Query(value="SELECT id, first_name AS firstName, surname AS surName, hospital_number AS hospitalNumber, sex " +
            "FROM patient_person WHERE uuid=?1", nativeQuery = true)
    Optional<ClientIdentificationProject> getBiometricPersonData(String personUuid);


    @Modifying
    @Transactional
    @Query("UPDATE Biometric b SET b.recapture = 0 WHERE b.recapture IS NULL")
    void updateRecaptureNullField();

    @Query(value = "SELECT recapture, COUNT(*) FROM biometric WHERE person_uuid = ?1 and archived = 0 GROUP BY recapture", nativeQuery = true)
    List<GroupedCapturedBiometric> getPatientBiometricCount(String personUuid);
}
