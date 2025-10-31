package com.cybersigma.sigmaverify.User.dto;

import lombok.Data;

@Data
public class ITRProfileResponse {
    private int code;
    private String message;
    private ITRProfileResult result;

    @Data
    public static class ITRProfileResult {
        private Address address;
        private Pan pan;
        private Contact contact;
        private Jurisdiction jurisdiction;
        private Aadhaar aadhaar;
    }

    @Data
    public static class Address {
        private String country;
        private String door_number;
        private String street;
        private Integer pin_code;
        private String zip_code;
        private String locality;
        private String post_office;
        private String city;
        private String state;
    }

    @Data
    public static class Pan {
        private String pan;
        private String name;
        private Dob dob;
        private String gender;
        private String category;
        private String address;
        private String status;
        private String indian_citizen;
    }

    @Data
    public static class Dob {
        private String date;
    }

    @Data
    public static class Contact {
        private Boolean resident;
        private Boolean non_resident;
        private String primary_mobile;
        private String primary_mobile_belongs_to;
        private String primary_email;
        private String primary_email_belongs_to;
        private String secondary_mobile;
        private String secondary_mobile_belongs_to;
        private String secondary_email;
        private String secondary_email_belongs_to;
    }

    @Data
    public static class Jurisdiction {
        private String area_code;
        private String ao_type;
        private String range_code;
        private String ao_number;
        private String jurisdiction;
        private String building_name;
        private String email_id;
        private String ao_building_id;
    }

    @Data
    public static class Aadhaar {
        private String aadhaar_number;
        private String aadhaar_status;
    }
}
