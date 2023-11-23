package org.lamisplus.modules.biometric.util;

public class Queries {

    public static class Bio{

        public static class BioQuery{

            public static String QUERIES = "SELECT id, first_name AS firstName, surname AS surName, hospital_number AS hospitalNumber, sex FROM patient_person WHERE uuid=?1";
        }
    }
}
