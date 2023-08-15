package org.lamisplus.modules.biometric.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NdrPatientCurrentTreatmentDetail {
	private String artStartDate;
	private String datimCode;
	private String daysOfArvRefill;
	private String dob;
	private String facilityName;
	private String hospitalNumber;
	private String lastDrugPickupDate;
	private String lastRegimen;
	private String lastViralLoadDate;
	private String lastViralLoadResult;
	private String lgaName;
	private String patientId;
	private String patientIdentifier;
	private String sex;
	private String stateName;
	private String subjectId;
}
