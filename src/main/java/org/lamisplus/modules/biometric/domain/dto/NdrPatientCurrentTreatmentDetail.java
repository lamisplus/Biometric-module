package org.lamisplus.modules.biometric.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NdrPatientCurrentTreatmentDetail {
	private String artStartDate;
	private String facilityId;
	private String daysOfArvRefill;
	private String dateOfBirth;
	private String facilityName;
	private String hospitalNumber;
	private String lastDrugPickupDate;
	private String lastRegimen;
	private String lastDrugRegimenCode;
	private String lastViralLoadDate;
	private String lastViralLoadResult;
	private String lgaName;
	private String patientId;
	private String patientIdentifier;
	private String sex;
	private String stateName;
	private String subjectId;
	
//	        "stateName": "Benue",
//			"facilityName": "Johnson Hospital",
//			"facilityId": "jfgdtI790e",
//			"patientIdentifier": "PB-16-3kUY",
//			"sex": "F",
//			"dateOfBirth": "5/7/1996",
//			"lastDrugPickupDate": null,
//			"lastDrugRegimen": null,
//			"lastDrugRegimenCode": null
}
