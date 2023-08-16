package org.lamisplus.modules.biometric.domain;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "biomettric_pims_tracker")
@SQLDelete(sql = "delete from biomettric_pims_tracker where id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "archived = 0")
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
public class PimsTracker  extends BiometricAuditEntity  implements Serializable, Persistable<String> {
	@Id
	@GeneratedValue( generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Basic(optional = false)
	@Column(name = "ID")
	private String id;
	
	@Column(name = "person_uuid")
	private String personUuid;
	
	@Column(name = "pims_patient_id")
	private String pimsPatientId;
	
	@Column(name = "verification_date")
	@NotNull
	private LocalDate date;
	
	@Column(name = "verification")
	private Boolean isVerified;
	
	@Type(type = "jsonb-node")
	@Column(columnDefinition = "jsonb")
	private JsonNode data;
	
	private Long facilityId;
	
	private Integer archived = 0;
	
	@Override
	public boolean isNew() {
		return id == null;
	}
}
