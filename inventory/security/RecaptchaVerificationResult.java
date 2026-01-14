package ru.kurs.inventory.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecaptchaVerificationResult(
        boolean success,
        String hostname,
        @JsonProperty("challenge_ts")
        String challengeTs,
        @JsonProperty("error-codes")
        List<String> errorCodes
) {
}
