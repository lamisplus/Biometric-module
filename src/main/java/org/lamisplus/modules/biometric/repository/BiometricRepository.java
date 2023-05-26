package org.lamisplus.modules.biometric.repository;

import org.lamisplus.modules.biometric.domain.Biometric;
import org.lamisplus.modules.biometric.domain.dto.GroupedCapturedBiometric;
import org.lamisplus.modules.biometric.domain.dto.StoredBiometric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BiometricRepository extends JpaRepository<Biometric, String> {
    List<Biometric> findAllByPersonUuid(String personUuid);

    @Query(value ="SELECT DISTINCT recapture FROM biometric WHERE person_uuid=?1", nativeQuery = true)
    List<String> findAllByPersonUuidAndRecaptures(String personUuid);

    @Query(value ="SELECT recapture FROM biometric WHERE person_uuid=?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<Integer> findMaxRecapture(String personUuid);

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


    @Query(value="SELECT person_uuid AS patientId, string_agg((CASE template_type WHEN 'Right Middle Finger' THEN template END), '') AS rightMiddleFinger,   \n" +
            "                string_agg((CASE template_type WHEN 'Right Thumb' THEN template END), '') AS rightThumb,  \n" +
            "            string_agg((CASE template_type WHEN 'Right Index Finger' THEN template END), '') AS rightIndexFinger,  \n" +
            "            string_agg((CASE template_type WHEN 'Right Ring Finger' THEN template END), '') AS rightRingFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Right Little Finger' THEN template END), '') AS rightLittleFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Left Index Finger' THEN template END), '') AS leftIndexFinger,   \n" +
            "            string_agg((CASE template_type WHEN 'Left Middle Finger' THEN template END), '') AS leftMiddleFinger,  \n" +
            "            string_agg((CASE template_type WHEN 'Left Thumb' THEN template END), '') AS leftThumb, \n" +
            "            string_agg((CASE template_type WHEN 'Left Ring Finger' THEN template END), '') AS leftRingFinger, \n" +
            "            string_agg((CASE template_type WHEN 'Left Little Finger' THEN template END), '') AS leftLittleFinger \n" +
            "            From biometric WHERE facility_id=?1 AND ENCODE(CAST(template AS BYTEA), 'hex') LIKE ?2 AND archived=0 Group By person_uuid", nativeQuery = true)
    Set<StoredBiometric> findByFacilityIdWithTemplate(Long facilityId, String template);


    @Query(value="SELECT uuid FROM patient_person WHERE id=?1", nativeQuery = true)
    Optional<String> getPersonUuid(Long patientId);

    @Query(value="SELECT MAX(b.enrollment_date) AS captureDate, b.person_uuid AS personUuid, " +
            "b.recapture, b.count, b.archived " +
            "FROM biometric b " +
            "INNER JOIN patient_person pp ON pp.uuid=b.person_uuid " +
            "WHERE pp.id=?1 AND b.archived != 1 AND pp.archived=0 " +
            "GROUP by b.person_uuid, b.recapture, b.count, b.archived ORDER BY b.recapture DESC", nativeQuery = true)
    List<GroupedCapturedBiometric> FindAllGroupedPersonBiometric(Long patientId);
}
