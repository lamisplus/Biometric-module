package org.lamisplus.modules.biometric.domain;


import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashMap;

@Entity
@Table(name = "deduplication")
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@TypeDefs({
        @TypeDef(name = "string-array", typeClass = StringArrayType.class),
        @TypeDef(name = "int-array", typeClass = IntArrayType.class),
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
        @TypeDef(name = "jsonb-node", typeClass = JsonNodeBinaryType.class),
        @TypeDef(name = "json-node", typeClass = JsonNodeStringType.class),
})
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

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "details", nullable = false)
    private Object details;

    @Transient
    private HashMap<String, String> mapDetails = new HashMap<>();
}
