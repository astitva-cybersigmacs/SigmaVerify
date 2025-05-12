package com.cybersigma.sigmaverify.utils;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequestDTO {

    private int start = 0;
    private int size = 25;

    private String projectId;

    private Date dateFrom;

    private Date dateTo;

    private String keyword;
    private String framework = "";

    private boolean status;
    private String actionStatus = "";
    private int userId;
    private String userName;
    private String state;

    public int getStart() {
        if (start < 1)
            return 0;
        return start;
    }

    public int getSize() {
        if (size < 0 || size > 25)
            return 25;
        return size;
    }
}
