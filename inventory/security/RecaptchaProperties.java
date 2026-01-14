package ru.kurs.inventory.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "recaptcha")
public class RecaptchaProperties {

    /** Public key used in browser. */
    private String siteKey;

    /** Secret key used for server-side verification. */
    private String secretKey;

    /** Optional: expected hostname returned by Google (to prevent token reuse on other domains). */
    private String expectedHostname;

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getExpectedHostname() {
        return expectedHostname;
    }

    public void setExpectedHostname(String expectedHostname) {
        this.expectedHostname = expectedHostname;
    }
}
