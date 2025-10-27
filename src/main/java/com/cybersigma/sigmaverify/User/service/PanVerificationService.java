package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.InvinciblePanResponse;
import com.cybersigma.sigmaverify.User.dto.PanRequestDTO;
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
public class PanVerificationService {

    private final RestTemplate restTemplate;
    private final InvincibleApiProperties props;
    private final ObjectMapper objectMapper  = new ObjectMapper();

    public InvinciblePanResponse verifyPan(String panNumber) {
        String url = props.getApi().getEndpoints().getPan();
        PanRequestDTO body = new PanRequestDTO(panNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("clientId", props.getClientId());
        headers.set("secretKey", props.getSecretKey());

        HttpEntity<PanRequestDTO> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<InvinciblePanResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, InvinciblePanResponse.class);
            return resp.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            InvinciblePanResponse err = new InvinciblePanResponse();
            err.setCode(ex.getRawStatusCode());
            String respBody = ex.getResponseBodyAsString();
            err.setMessage(respBody != null ? respBody : ex.getStatusText());
            return err;
        } catch (Exception e) {
            InvinciblePanResponse err = new InvinciblePanResponse();
            err.setCode(500);
            err.setMessage("Internal error: " + e.getMessage());
            return err;
        }
    }
}


