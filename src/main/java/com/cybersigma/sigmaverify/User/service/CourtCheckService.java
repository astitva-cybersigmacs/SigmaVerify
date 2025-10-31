package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.CourtCheckRequestDTO;
import com.cybersigma.sigmaverify.User.dto.InvincibleCourtCheckResponse;
import com.cybersigma.sigmaverify.security.InvincibleApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourtCheckService {

    private final RestTemplate restTemplate;
    private final InvincibleApiProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InvincibleCourtCheckResponse checkCourtCase(String cnrNumber) {
        String url = props.getApi().getEndpoints().getCourtCheck();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("clientId", props.getClientId());
        headers.set("secretKey", props.getSecretKey());

        CourtCheckRequestDTO requestBody = new CourtCheckRequestDTO(cnrNumber);
        HttpEntity<CourtCheckRequestDTO> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Checking court case for CNR: {}", cnrNumber);

            ResponseEntity<InvincibleCourtCheckResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, InvincibleCourtCheckResponse.class);

            InvincibleCourtCheckResponse response = resp.getBody();

            if (response != null && response.getResult() != null) {
                log.info("Court case check response - Success: {}, Message: {}",
                        response.getResult().isSuccess(),
                        response.getResult().getMessage());

                if (response.getResult().getData() != null &&
                        response.getResult().getData().getCnr_details() != null) {

                    InvincibleCourtCheckResponse.CaseStatus caseStatus =
                            response.getResult().getData().getCnr_details().getCase_status();

                    if (caseStatus != null) {
                        log.info("Case Status - Next Hearing: {}, Decision Date: {}, Disposal: {}",
                                caseStatus.getNext_hearing_date(),
                                caseStatus.getDecision_date(),
                                caseStatus.getNature_of_disposal());
                    }
                }
            }

            return response;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Court check failed with HTTP error: {}", ex.getStatusCode());
            InvincibleCourtCheckResponse err = new InvincibleCourtCheckResponse();
            err.setCode(ex.getRawStatusCode());

            InvincibleCourtCheckResponse.CourtCheckResult result =
                    new InvincibleCourtCheckResponse.CourtCheckResult();
            result.setSuccess(false);
            result.setMessage(ex.getResponseBodyAsString() != null ?
                    ex.getResponseBodyAsString() : ex.getStatusText());
            err.setResult(result);

            return err;

        } catch (Exception e) {
            log.error("Court check failed with exception: {}", e.getMessage(), e);
            InvincibleCourtCheckResponse err = new InvincibleCourtCheckResponse();
            err.setCode(500);

            InvincibleCourtCheckResponse.CourtCheckResult result =
                    new InvincibleCourtCheckResponse.CourtCheckResult();
            result.setSuccess(false);
            result.setMessage("Internal error: " + e.getMessage());
            err.setResult(result);

            return err;
        }
    }
}
