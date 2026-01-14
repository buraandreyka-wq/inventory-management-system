package ru.kurs.inventory.security;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class RecaptchaVerifier {

    private final RecaptchaProperties properties;
    private final RestClient restClient;

    public RecaptchaVerifier(RecaptchaProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl("https://www.google.com/recaptcha/api")
                .build();
    }

    public boolean verify(String token, String remoteIp) {
        if (token == null || token.isBlank()) {
            return false;
        }
        if (properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            // If secret key is not configured - fail safe.
            return false;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", properties.getSecretKey());
        form.add("response", token);
        if (remoteIp != null && !remoteIp.isBlank()) {
            form.add("remoteip", remoteIp);
        }

        RecaptchaVerificationResult result = restClient
                .post()
                .uri("/siteverify")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(RecaptchaVerificationResult.class);

        if (result == null || !result.success()) {
            return false;
        }

        String expectedHostname = properties.getExpectedHostname();
        if (expectedHostname != null && !expectedHostname.isBlank()) {
            return expectedHostname.equalsIgnoreCase(result.hostname());
        }

        return true;
    }
}
