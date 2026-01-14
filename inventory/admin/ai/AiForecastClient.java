package ru.kurs.inventory.admin.ai;

import ru.kurs.inventory.admin.ai.dto.AiForecastRequest;
import ru.kurs.inventory.admin.ai.dto.AiForecastResponse;

public interface AiForecastClient {

    AiForecastResponse forecast(AiForecastRequest request);
}
