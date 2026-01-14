package ru.kurs.inventory.admin.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.kurs.inventory.admin.ai.dto.AiForecastRequest;
import ru.kurs.inventory.admin.ai.dto.AiForecastResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback-реализация, если внешний AI сервис не подключён.
 *
 * Делает простой прогноз: берёт средний спрос по истории и умножает на forecastDays.
 */
@Component
@ConditionalOnProperty(prefix = "ai.forecast", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockAiForecastClient implements AiForecastClient {

    @Override
    public AiForecastResponse forecast(AiForecastRequest request) {
        AiForecastResponse resp = new AiForecastResponse();
        resp.setForecastDays(request.getForecastDays());

        List<AiForecastResponse.ProductForecast> items = new ArrayList<>();
        if (request.getProducts() != null) {
            for (AiForecastRequest.ProductSeries p : request.getProducts()) {
                long total = 0;
                int n = 0;
                if (p.getHistory() != null) {
                    for (AiForecastRequest.DailyPoint point : p.getHistory()) {
                        total += Math.max(0, point.getDemandQty());
                        n++;
                    }
                }

                double avg = n == 0 ? 0.0 : (double) total / (double) n;

                // Делаем "псевдо" прогноз по дням: равномерно распределяем средний спрос.
                int fd = Math.max(1, request.getForecastDays());
                List<AiForecastResponse.DailyForecastPoint> daily = new ArrayList<>();
                java.time.LocalDate start = java.time.LocalDate.now().plusDays(1);
                for (int i = 0; i < fd; i++) {
                    long v = Math.round(avg);
                    daily.add(new AiForecastResponse.DailyForecastPoint(start.plusDays(i).toString(), Math.max(0, v)));
                }

                long forecastTotal = daily.stream().mapToLong(AiForecastResponse.DailyForecastPoint::getDemandQty).sum();

                AiForecastResponse.ProductForecast pf = new AiForecastResponse.ProductForecast(p.getProductId(), forecastTotal, avg);
                pf.setDaily(daily);
                items.add(pf);
            }
        }

        resp.setProducts(items);
        return resp;
    }
}
