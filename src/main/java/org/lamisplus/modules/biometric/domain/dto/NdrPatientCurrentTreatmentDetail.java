package org.lamisplus.modules.biometric.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NdrPatientCurrentTreatmentDetail {
	private String nin;
	private String clientId;
	private String patientId;
	private String patientIdentifier;
	private String sex;
	private String age;
	private String dateOfBirth;
	private String facilityId;
	private String lastDrugRegimenCode;
	private String weight;
	private String lastClinicalVisitDate;
	private String lastDocumentedBP;
	private String daysOfArvRefill;
	private String lastViralLoadResult;
	private String lastViralLoadDate;
	private String pregnancyStatus;
	private String artStartDate;
	private String nextAppointmentDate;
	private String facilityName;
	private String hospitalNumber;
	private String lastDrugPickupDate;
	private String lastRegimen;
	private String state;
	private String lga;
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
