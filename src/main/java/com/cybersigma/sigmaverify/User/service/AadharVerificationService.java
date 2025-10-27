package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.AadharRequestDTO;
import com.cybersigma.sigmaverify.User.dto.InvincibleAadharResponse;
import com.cybersigma.sigmaverify.security.InvincibleApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;

@Service
@RequiredArgsConstructor
public class AadharVerificationService {

    private final RestTemplate restTemplate;
    private final InvincibleApiProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InvincibleAadharResponse verifyAadhar(String uid) {
        String url = props.getApi().getEndpoints().getAadhaar();
        AadharRequestDTO body = new AadharRequestDTO(uid);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("clientId", props.getClientId());
        headers.set("secretKey", props.getSecretKey());

        HttpEntity<AadharRequestDTO> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<InvincibleAadharResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, InvincibleAadharResponse.class);
            return resp.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // provider returned 4xx / 5xx; forward response body as message
            InvincibleAadharResponse err = new InvincibleAadharResponse();
            err.setCode(ex.getRawStatusCode());
            String respBody = ex.getResponseBodyAsString();
            // raw string as message (no parsing)
            err.setMessage(respBody != null ? respBody : ex.getStatusText());
            return err;
        } catch (Exception e) {
            InvincibleAadharResponse err = new InvincibleAadharResponse();
            err.setCode(500);
            err.setMessage("Internal error: " + e.getMessage());
            return err;
        }
    }
}

