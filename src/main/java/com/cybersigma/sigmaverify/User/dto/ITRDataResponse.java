package com.cybersigma.sigmaverify.User.dto;

import lombok.Data;
import java.util.List;

@Data
public class ITRDataResponse {
    private int code;
    private String message;
    private ITRDataResult result;

    @Data
    public static class ITRDataResult {
        private String pan_no;
        private List<FiledITR> filed_itrs;
    }

    @Data
    public static class FiledITR {
        private String itr_id;
        private String pan_no;
        private String filing_year;
        private String acknowledgement_no;
        private String itr_form;
        private String filing_type;
        private String filing_section;
        private String filing_date;
        private List<ITRStatus> itr_status;
        private String download_link;
    }

    @Data
    public static class ITRStatus {
        private String status;
        private String date;
    }
}
