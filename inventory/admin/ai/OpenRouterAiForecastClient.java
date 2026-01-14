package ru.kurs.inventory.admin.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;

@Component
@ConditionalOnProperty(prefix = "ai.forecast", name = "provider", havingValue = "openrouter")
public class OpenRouterAiForecastClient implements AiForecastClient {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterAiForecastClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final boolean jsonMode;

    private final int maxAttempts;

    public OpenRouterAiForecastClient(
            ObjectMapper objectMapper,
            @Value("${ai.forecast.openrouter.base-url:https://openrouter.ai/api/v1}") String baseUrl,
            @Value("${ai.forecast.openrouter.api-key:${OPENROUTER_API_KEY:}}") String apiKey,
            @Value("${ai.forecast.openrouter.model:allenai/olmo-3.1-32b-think}") String model,
            @Value("${ai.forecast.openrouter.json-mode:false}") boolean jsonMode,
            @Value("${ai.forecast.openrouter.max-attempts:2}") int maxAttempts,
            @Value("${ai.forecast.connect-timeout:5s}") String connectTimeout,
            @Value("${ai.forecast.read-timeout:30s}") String readTimeout
    ) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.jsonMode = jsonMode;
        this.maxAttempts = Math.max(1, maxAttempts);

        Duration ct = DurationStyle.detectAndParse(connectTimeout);
        Duration rt = DurationStyle.detectAndParse(readTimeout);

        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout((int) ct.toMillis());
        rf.setReadTimeout((int) rt.toMillis());

        this.restTemplate = new RestTemplate(rf);
    }

    @Override
    public AiForecastResponse forecast(AiForecastRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Не задан OPENROUTER_API_KEY (или ai.forecast.openrouter.api-key)");
        }

        String url = baseUrl + "/chat/completions";
        log.debug("Calling OpenRouter API: {} model={}", url, model);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "http://localhost:8080");
        headers.set("X-Title", "inventory");

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            // на повторе увеличим токены, чтобы не ловить finish_reason=length
            int maxTokens = attempt == 1 ? 800 : 1400;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(buildPayload(request, maxTokens), headers);

            try {
                ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                if (!resp.getStatusCode().is2xxSuccessful()) {
                    throw new IllegalStateException("OpenRouter вернул неожиданный ответ: " + resp.getStatusCode());
                }

                String body = resp.getBody();
                if (body == null || body.isBlank()) {
                    throw new IllegalStateException("OpenRouter вернул пустое тело ответа");
                }

                String assistantText = extractAssistantText(body);
                String finishReason = extractFinishReason(body);

                if (assistantText == null || assistantText.isBlank()) {
                    // у think-моделей content может быть пустым, но reasoning иногда тоже пустой
                    if (attempt < maxAttempts) {
                        log.warn("OpenRouter returned empty assistant text (attempt {}/{}). finish_reason={}",
                                attempt, maxAttempts, finishReason);
                        continue;
                    }
                    throw new IllegalStateException("OpenRouter вернул пустой ответ (message.content/reasoning пусты)");
                }

                // Если ответ обрезан, JSON может не закрыться — попробуем повтор
                if ("length".equalsIgnoreCase(finishReason)) {
                    String jsonMaybe = extractJsonObject(assistantText);
                    if ((jsonMaybe == null || jsonMaybe.isBlank()) && attempt < maxAttempts) {
                        log.warn("OpenRouter response is truncated (finish_reason=length). Retrying (attempt {}/{})",
                                attempt, maxAttempts);
                        continue;
                    }
                }

                String json = extractJsonObject(assistantText);

                // Если модель не послушалась и вернула обычный текст — пробуем "repair" запрос:
                // попросить модель преобразовать свой же ответ в строгий JSON.
                if (json == null || json.isBlank()) {
                    try {
                        String repaired = requestJsonRepair(url, headers, request, assistantText);
                        json = extractJsonObject(repaired);
                    } catch (Exception repairEx) {
                        log.warn("OpenRouter JSON repair failed: {}", repairEx.getMessage());
                        // оставляем json пустым — ниже сработает понятная ошибка
                    }
                }

                if (json == null || json.isBlank()) {
                    log.warn("OpenRouter returned non-JSON content. content-preview={}",
                            assistantText.length() > 500 ? assistantText.substring(0, 500) + "..." : assistantText);
                    throw new IllegalStateException("AI вернул ответ не в формате JSON. Попробуйте ещё раз или смените модель.");
                }

                try {
                    return objectMapper.readValue(json, AiForecastResponse.class);
                } catch (Exception parseEx) {
                    log.warn("Failed to parse AI JSON. json-preview={}",
                            json.length() > 500 ? json.substring(0, 500) + "..." : json);
                    throw new IllegalStateException("Не удалось распарсить ответ AI как JSON. Попробуйте ещё раз.");
                }
            } catch (Exception ex) {
                lastException = ex;

                // Если это явная ошибка OpenRouter (error/message), ретраить обычно бессмысленно.
                if (isOpenRouterError(ex)) {
                    break;
                }

                log.warn("OpenRouter attempt {}/{} failed: {}", attempt, maxAttempts, ex.getMessage());
                if (attempt >= maxAttempts) {
                    break;
                }
            }
        }

        log.warn("OpenRouter forecast failed", lastException);
        throw new IllegalStateException("Не удалось получить прогноз от AI. Проверьте ключ/лимиты OpenRouter и попробуйте ещё раз.");
    }

    private Map<String, Object> buildPayload(AiForecastRequest request, int maxTokens) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("temperature", 0.0);
        payload.put("max_tokens", maxTokens);
        payload.put("stream", false);

        // Не все модели/провайдеры в OpenRouter поддерживают строгий JSON-mode.
        // Если включить принудительно, некоторые модели отвечают ошибкой.
        // Поэтому включаем опционально: по умолчанию выключено.
        if (jsonMode) {
            payload.put("response_format", Map.of("type", "json_object"));
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content",
                "Ты сервис прогнозирования спроса. Твои ответы парсит программа. " +
                        "Верни ТОЛЬКО валидный JSON без markdown, без пояснений, без рассуждений, без шагов решения. " +
                        "Ответ ДОЛЖЕН начинаться с '{' и заканчиваться '}'. Никакого текста до/после. " +
                "Схема ответа: {\"forecastDays\":int, \"products\":[{\"productId\":number, \"forecastTotalDemand\":number, \"forecastAvgDaily\":number, \"daily\":[{\"day\":\"YYYY-MM-DD\",\"demandQty\":number}]}]}. " +
                        "daily желательно на каждый день горизонта forecastDays. " +
                        "Если нет истории, прогноз = 0."
        ));
        messages.add(Map.of(
                "role", "user",
                "content",
                "Сделай прогноз спроса на " + request.getForecastDays() + " дней. Входные данные (JSON):\n" +
                        safeJson(request)
        ));
        payload.put("messages", messages);

        return payload;
    }

    /**
     * "Ремонт" ответа: если модель вернула обычный текст/пояснения, делаем второй запрос
     * и просим вернуть строгий JSON по нужной схеме.
     */
    private String requestJsonRepair(String url,
                                    HttpHeaders headers,
                                    AiForecastRequest originalRequest,
                                    String originalAssistantText) throws Exception {

        String reqJson = safeJson(originalRequest);

        // Чтобы не улететь в лимиты контекста — ограничим вставку исходного ответа.
        String assistantSnippet = originalAssistantText == null ? "" : originalAssistantText.trim();
        final int limit = 6000;
        if (assistantSnippet.length() > limit) {
            assistantSnippet = assistantSnippet.substring(0, limit) + "\n...<truncated>...";
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("temperature", 0.0);
        payload.put("max_tokens", 1000);
        payload.put("stream", false);

        // В repair-запросе JSON-mode обычно полезен, но он может ломать модели, которые не поддерживают его.
        // Поэтому включаем только если он явно активирован настройкой.
        if (jsonMode) {
            payload.put("response_format", Map.of("type", "json_object"));
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content",
                "Ты конвертер ответа в строгий JSON. Верни ТОЛЬКО валидный JSON-объект без markdown и текста. " +
                        "Ответ ДОЛЖЕН начинаться с '{' и заканчиваться '}'. " +
                        "Схема: {\"forecastDays\":int, \"products\":[{\"productId\":number, \"forecastTotalDemand\":number, \"forecastAvgDaily\":number, \"daily\":[{\"day\":\"YYYY-MM-DD\",\"demandQty\":number}]}]}. " +
                        "forecastDays возьми из входного запроса. " +
                        "daily построй на forecastDays дней (если можешь), иначе можно пропустить daily. " +
                        "Если по товару данных недостаточно — поставь 0."
        ));
        messages.add(Map.of(
                "role", "user",
                "content",
                "Входные данные запроса (JSON):\n" + reqJson + "\n\n" +
                        "Исходный ответ модели (не JSON):\n" + assistantSnippet + "\n\n" +
                        "Сконвертируй в JSON строго по схеме."
        ));
        payload.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("OpenRouter вернул неожиданный ответ: " + resp.getStatusCode());
        }
        String body = resp.getBody();
        if (body == null || body.isBlank()) {
            throw new IllegalStateException("OpenRouter вернул пустое тело ответа (repair)");
        }

        return extractAssistantText(body);
    }

    private String safeJson(Object any) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(any);
        } catch (Exception e) {
            return String.valueOf(any);
        }
    }

    private boolean isOpenRouterError(Exception ex) {
        String msg = ex.getMessage();
        return msg != null && msg.startsWith("OpenRouter error:");
    }

    /**
     * Извлекаем текст ассистента.
     *
     * Важно: у некоторых моделей (особенно *-think) OpenRouter может вернуть content="",
     * а текст/JSON будет в message.reasoning.
     */
    private String extractAssistantText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        // OpenRouter иногда возвращает ошибку в теле с 200
        JsonNode error = root.path("error");
        if (!error.isMissingNode() && !error.isNull()) {
            String msg = error.path("message").asText("");
            String code = error.path("code").asText("");
            String suffix = code.isBlank() ? "" : " (code=" + code + ")";
            throw new IllegalStateException("OpenRouter error: " + msg + suffix);
        }

        JsonNode message = root.path("choices").path(0).path("message");
        if (message.isMissingNode() || message.isNull()) {
            throw new IllegalStateException("OpenRouter: не найден choices[0].message");
        }

        String contentText = extractTextFromNode(message.path("content"));
        if (contentText != null && !contentText.isBlank()) {
            return contentText;
        }

        // Legacy
        String text = extractTextFromNode(message.path("text"));
        if (text != null && !text.isBlank()) {
            return text;
        }

        // Think models
        String reasoning = extractTextFromNode(message.path("reasoning"));
        if (reasoning != null && !reasoning.isBlank()) {
            return reasoning;
        }

        // Tool calls: некоторые модели возвращают результат как arguments функции
        // при пустом message.content.
        String toolArgs = extractTextFromToolCalls(message.path("tool_calls"));
        if (toolArgs != null && !toolArgs.isBlank()) {
            return toolArgs;
        }

        // Legacy function_call (редко)
        String functionArgs = extractTextFromNode(message.path("function_call").path("arguments"));
        if (functionArgs != null && !functionArgs.isBlank()) {
            return functionArgs;
        }

        return "";
    }

    private String extractTextFromToolCalls(JsonNode toolCallsNode) throws Exception {
        if (toolCallsNode == null || toolCallsNode.isMissingNode() || toolCallsNode.isNull() || !toolCallsNode.isArray()) {
            return "";
        }
        if (toolCallsNode.isEmpty()) {
            return "";
        }

        JsonNode first = toolCallsNode.get(0);
        if (first == null || first.isNull()) {
            return "";
        }

        // OpenAI style: tool_calls[0].function.arguments
        JsonNode args = first.path("function").path("arguments");
        String argsText = extractTextFromNode(args);
        if (argsText != null && !argsText.isBlank()) {
            return argsText;
        }

        // Fallback: tool_calls[0].arguments
        argsText = extractTextFromNode(first.path("arguments"));
        return argsText == null ? "" : argsText;
    }

    private String extractFinishReason(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").path(0).path("finish_reason").asText("");
        } catch (Exception e) {
            return "";
        }
    }

    private String extractTextFromNode(JsonNode node) throws Exception {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }

        // Обычно строка
        if (node.isTextual()) {
            return node.asText();
        }

        // Иногда массив частей [{type:"text", text:"..."}, ...]
        if (node.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode part : node) {
                if (part == null || part.isNull()) continue;
                JsonNode text = part.path("text");
                if (text.isTextual()) {
                    sb.append(text.asText());
                }
            }
            return sb.toString();
        }

        // Если вдруг объект — сериализуем
        return objectMapper.writeValueAsString(node);
    }

    /**
     * Защита от случаев, когда модель оборачивает JSON в текст/код-фенсы.
     */
    private static String extractJsonObject(String text) {
        if (text == null) return "";
        String t = text.trim();

        // Убираем ```json ... ``` если вдруг пришло
        if (t.startsWith("```")) {
            int firstNl = t.indexOf('\n');
            if (firstNl > 0) {
                t = t.substring(firstNl + 1);
            }
            int end = t.lastIndexOf("```");
            if (end >= 0) {
                t = t.substring(0, end);
            }
            t = t.trim();
        }

        int start = t.indexOf('{');
        int end = t.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return t.substring(start, end + 1);
        }

        // Если фигурных скобок нет — это не JSON-объект
        return "";
    }
}
