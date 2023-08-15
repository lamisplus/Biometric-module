package org.lamisplus.modules.biometric.domain;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "deduplication")
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Deduplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "deduplication_date")
    private LocalDate DeduplicationDate;

    @Column(name = "unmatched_count")
    private Integer unmatchedCount=0;

    @Column(name = "matched_count")
    private Integer matchedCount=0;

    @Column(name = "person_uuid")
    private String personUuid;

    @Column(name = "baseline_finger_count")
    private Integer baselineFingerCount=0;

    @Column(name = "recapture_finger_count")
    private Integer recaptureFingerCount=0;

    @Column(name = "perfect_match_count")
    private Integer perfectMatchCount=0;

    @Column(name = "imperfect_match_count")
    private Integer imperfectMatchCount=0;
}
