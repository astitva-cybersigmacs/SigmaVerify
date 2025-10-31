package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.BankAccountRequestDTO;
import com.cybersigma.sigmaverify.User.dto.InvincibleBankAccountResponse;
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
public class BankAccountVerificationService {

    private final RestTemplate restTemplate;
    private final InvincibleApiProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InvincibleBankAccountResponse verifyBankAccount(BankAccountRequestDTO request) {
        String url = props.getApi().getEndpoints().getBankAccount();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("clientId", props.getClientId());
        headers.set("secretKey", props.getSecretKey());

        HttpEntity<BankAccountRequestDTO> entity = new HttpEntity<>(request, headers);

        try {
            log.info("Verifying bank account: {} with IFSC: {}",
                    maskAccountNumber(request.getBankAccount()), request.getIfsc());

            ResponseEntity<InvincibleBankAccountResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, InvincibleBankAccountResponse.class);

            InvincibleBankAccountResponse response = resp.getBody();

            if (response != null && response.getResult() != null) {
                log.info("Bank account verification response - Status: {}, AccountStatus: {}",
                        response.getResult().getStatus(),
                        response.getResult().getAccountStatus());
            }

            return response;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Bank account verification failed with HTTP error: {}", ex.getStatusCode());
            InvincibleBankAccountResponse err = new InvincibleBankAccountResponse();
            err.setCode(ex.getRawStatusCode());
            String respBody = ex.getResponseBodyAsString();
            err.setMessage(respBody != null ? respBody : ex.getStatusText());
            return err;

        } catch (Exception e) {
            log.error("Bank account verification failed with exception: {}", e.getMessage(), e);
            InvincibleBankAccountResponse err = new InvincibleBankAccountResponse();
            err.setCode(500);
            err.setMessage("Internal error: " + e.getMessage());
            return err;
        }
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        int visibleDigits = 4;
        String masked = "*".repeat(accountNumber.length() - visibleDigits);
        return masked + accountNumber.substring(accountNumber.length() - visibleDigits);
    }
}
