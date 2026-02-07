package com.gler.assignment.services;

import com.gler.assignment.dto.ForecastRequest;
import com.gler.assignment.dto.ForecastResponse;

public interface ForecastService {
    ForecastResponse processForecast(ForecastRequest request);
}