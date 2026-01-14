package ru.kurs.inventory.security;

import org.springframework.security.core.AuthenticationException;

public class RecaptchaAuthenticationException extends AuthenticationException {

    public RecaptchaAuthenticationException(String msg) {
        super(msg);
    }
}
