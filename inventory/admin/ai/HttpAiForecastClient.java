package ru.kurs.inventory.admin.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.kurs.inventory.admin.ai.dto.AiForecastRequest;
import ru.kurs.inventory.admin.ai.dto.AiForecastResponse;

import java.time.Duration;

/**
 * Клиент внешнего AI/ML API.
 *
 * Подход: внешний HTTP API (можно заменить на реальный ML микросервис).
 */
@Component
@ConditionalOnProperty(prefix = "ai.forecast", name = "provider", havingValue = "http")
public class HttpAiForecastClient implements AiForecastClient {

    private static final Logger log = LoggerFactory.getLogger(HttpAiForecastClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public HttpAiForecastClient(
            @Value("${ai.forecast.base-url:http://localhost:8099}") String baseUrl,
            @Value("${ai.forecast.api-key:}") String apiKey,
            @Value("${ai.forecast.connect-timeout:2s}") String connectTimeout,
            @Value("${ai.forecast.read-timeout:10s}") String readTimeout
    ) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;

        Duration ct = DurationStyle.detectAndParse(connectTimeout);
        Duration rt = DurationStyle.detectAndParse(readTimeout);

        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout((int) ct.toMillis());
        rf.setReadTimeout((int) rt.toMillis());

        this.restTemplate = new RestTemplate(rf);
    }

    @Override
    public AiForecastResponse forecast(AiForecastRequest request) {
        String url = baseUrl + "/v1/forecast";
        log.info("Calling external AI forecast API: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("X-API-KEY", apiKey);
        } else {
            // Явный сигнал в логах: часто причина "пустой страницы" — забыли задать ключ.
            log.warn("AI_FORECAST_API_KEY (ai.forecast.api-key) is empty; request will be sent without X-API-KEY");
        }

        HttpEntity<AiForecastRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<AiForecastResponse> resp = restTemplate.exchange(url, HttpMethod.POST, entity, AiForecastResponse.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new IllegalStateException("AI API вернул неожиданный ответ: " + resp.getStatusCode());
            }
            return resp.getBody();
        } catch (Exception ex) {
            log.warn("AI forecast API call failed: {}", ex.getMessage());
            throw new IllegalStateException("Не удалось получить прогноз от AI сервиса: " + ex.getMessage(), ex);
        }
    }
}
