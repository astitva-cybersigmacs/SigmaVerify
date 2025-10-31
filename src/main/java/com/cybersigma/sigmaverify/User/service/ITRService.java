package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.*;
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
public class ITRService {

    private final RestTemplate restTemplate;
    private final InvincibleApiProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Step 1: Generate Client ID by login
     */
    public ITRLoginResponse generateClientId(String username, String password) {
        String url = props.getApi().getEndpoints().getItrLogin();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("clientId", props.getClientId());
        headers.set("secretKey", props.getSecretKey());

        ITRLoginRequestDTO requestBody = new ITRLoginRequestDTO(username, password);
        HttpEntity<ITRLoginRequestDTO> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Generating ITR Client ID for username: {}", username);

            ResponseEntity<ITRLoginResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, ITRLoginResponse.class);

            ITRLoginResponse response = resp.getBody();

            if (response != null && response.getCode() == 200) {
                log.info("ITR Client ID generated successfully: {}",
                        maskClientId(response.getClient_id()));
            } else {
                log.warn("ITR Client ID generation failed. Code: {}",
                        response != null ? response.getCode() : "null");
            }

            return response;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("ITR login failed with HTTP error: {}", ex.getStatusCode());
            ITRLoginResponse err = new ITRLoginResponse();
            err.setCode(ex.getRawStatusCode());
            err.setMessage(ex.getResponseBodyAsString() != null ?
                    ex.getResponseBodyAsString() : ex.getStatusText());
            return err;

        } catch (Exception e) {
            log.error("ITR login failed with exception: {}", e.getMessage(), e);
            ITRLoginResponse err = new ITRLoginResponse();
            err.setCode(500);
            err.setMessage("Internal error: " + e.getMessage());
            return err;
        }
    }

    /**
     * Step 2: Download ITR Profile
     */
    public ITRProfileResponse downloadProfile(String clientId) {
        String url = props.getApi().getEndpoints().getItrProfile();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("clientId", props.getClientId());
        headers.set("secretKey", props.getSecretKey());

        ITRProfileRequestDTO requestBody = new ITRProfileRequestDTO(clientId);
        HttpEntity<ITRProfileRequestDTO> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Downloading ITR profile for client ID: {}", maskClientId(clientId));

            ResponseEntity<ITRProfileResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, ITRProfileResponse.class);

            ITRProfileResponse response = resp.getBody();

            if (response != null && response.getCode() == 200) {
                log.info("ITR Profile downloaded successfully");
                if (response.getResult() != null && response.getResult().getPan() != null) {
                    log.info("PAN: {}, Name: {}",
                            response.getResult().getPan().getPan(),
                            response.getResult().getPan().getName());
                }
            } else {
                log.warn("ITR Profile download failed. Code: {}, Message: {}",
                        response != null ? response.getCode() : "null",
                        response != null ? response.getMessage() : "null");
            }

            return response;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("ITR profile download failed with HTTP error: {}", ex.getStatusCode());
            ITRProfileResponse err = new ITRProfileResponse();
            err.setCode(ex.getRawStatusCode());
            err.setMessage(ex.getResponseBodyAsString() != null ?
                    ex.getResponseBodyAsString() : ex.getStatusText());
            return err;

        } catch (Exception e) {
            log.error("ITR profile download failed with exception: {}", e.getMessage(), e);
            ITRProfileResponse err = new ITRProfileResponse();
            err.setCode(500);
            err.setMessage("Internal error: " + e.getMessage());
            return err;
        }
    }

    /**
     * Step 3: Download ITR Data (all filed ITRs)
     */
    public ITRDataResponse downloadITRData(String clientId) {
        String url = props.getApi().getEndpoints().getItrData();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("clientId", props.getClientId());
        headers.set("secretKey", props.getSecretKey());

        ITRDataRequestDTO requestBody = new ITRDataRequestDTO(clientId);
        HttpEntity<ITRDataRequestDTO> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Downloading ITR data for client ID: {}", maskClientId(clientId));

            ResponseEntity<ITRDataResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, ITRDataResponse.class);

            ITRDataResponse response = resp.getBody();

            if (response != null && response.getCode() == 200) {
                int itrCount = response.getResult() != null &&
                        response.getResult().getFiled_itrs() != null ?
                        response.getResult().getFiled_itrs().size() : 0;
                log.info("ITR Data downloaded successfully. Total ITRs: {}", itrCount);
            } else {
                log.warn("ITR Data download failed. Code: {}, Message: {}",
                        response != null ? response.getCode() : "null",
                        response != null ? response.getMessage() : "null");
            }

            return response;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("ITR data download failed with HTTP error: {}", ex.getStatusCode());
            ITRDataResponse err = new ITRDataResponse();
            err.setCode(ex.getRawStatusCode());
            err.setMessage(ex.getResponseBodyAsString() != null ?
                    ex.getResponseBodyAsString() : ex.getStatusText());
            return err;

        } catch (Exception e) {
            log.error("ITR data download failed with exception: {}", e.getMessage(), e);
            ITRDataResponse err = new ITRDataResponse();
            err.setCode(500);
            err.setMessage("Internal error: " + e.getMessage());
            return err;
        }
    }

    private String maskClientId(String clientId) {
        if (clientId == null || clientId.length() <= 8) {
            return "****";
        }
        return clientId.substring(0, 4) + "****" + clientId.substring(clientId.length() - 4);
    }
}
