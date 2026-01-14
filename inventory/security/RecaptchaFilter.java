package ru.kurs.inventory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates Google reCAPTCHA v2 token on login form submit.
 *
 * Must be placed before UsernamePasswordAuthenticationFilter.
 */
public class RecaptchaFilter extends OncePerRequestFilter {

    public static final String RECAPTCHA_RESPONSE_PARAM = "g-recaptcha-response";

    private final RecaptchaVerifier verifier;

    public RecaptchaFilter(RecaptchaVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if ("/login".equals(request.getServletPath()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String token = request.getParameter(RECAPTCHA_RESPONSE_PARAM);
            String remoteIp = request.getRemoteAddr();

            boolean ok = verifier.verify(token, remoteIp);
            if (!ok) {
                String msg = "Пожалуйста, подтвердите, что вы не робот";
                String encoded = java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8);
                response.sendRedirect("/login?captchaError=" + encoded);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
