package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.InvinciblePanResponse;
import com.cybersigma.sigmaverify.User.dto.PanRequestDTO;
import com.cybersigma.sigmaverify.security.InvincibleApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
            ResponseEntity<String> raw = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);
            log.info("Invincible API raw response: {}", raw.getBody());
            InvinciblePanResponse resp = objectMapper.readValue(raw.getBody(), InvinciblePanResponse.class);
            return resp;        } catch (HttpClientErrorException | HttpServerErrorException ex) {
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


