package com.cybersigma.sigmaverify.User.dto;

import lombok.Data;
import java.util.List;

@Data
public class InvincibleCourtCheckResponse {
    private int code;
    private CourtCheckResult result;

    @Data
    public static class CourtCheckResult {
        private CourtCheckData data;
        private int status_code;
        private boolean success;
        private String message;
        private String message_code;
    }

    @Data
    public static class CourtCheckData {
        private String cnr_number;
        private CnrDetails cnr_details;
    }

    @Data
    public static class CnrDetails {
        private CaseDetails case_details;
        private CaseStatus case_status;
        private PetitionerAndAdvocateDetails petitioner_and_advocate_details;
        private List<String> respondent_and_advocate_details;
        private List<ActDetail> act_details;
        private Object subordinate_court_information_details;
        private List<CaseHistoryDetail> case_history_details;
        private List<OrderDetail> interim_orders_details;
        private List<OrderDetail> final_orders_and_judgements_details;
        private List<CaseTransferDetail> case_transfer_and_establishment_details;
        private List<Object> process_details;
    }

    @Data
    public static class CaseDetails {
        private String case_type;
        private String filing_number;
        private String filing_date;
        private String registration_number;
        private String registration_date;
    }

    @Data
    public static class CaseStatus {
        private String first_hearing_date;
        private String next_hearing_date;
        private String case_stage;
        private String court_number_and_judge;
        private String decision_date;
        private String nature_of_disposal;
    }

    @Data
    public static class PetitionerAndAdvocateDetails {
        private String petitioner;
        private String advocate;
    }

    @Data
    public static class ActDetail {
        private String under_act;
        private String under_section;
    }

    @Data
    public static class CaseHistoryDetail {
        private String judge;
        private String business_on_date;
        private String hearing_date;
        private String purpose_of_hearing;
    }

    @Data
    public static class OrderDetail {
        private String order_number;
        private String order_date;
    }

    @Data
    public static class CaseTransferDetail {
        private String transfer_date;
        private String from_court_number_and_judge;
        private String to_court_number_and_judge;
    }
}
