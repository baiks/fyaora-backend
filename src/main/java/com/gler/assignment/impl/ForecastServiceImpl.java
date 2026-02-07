package com.gler.assignment.impl;

import com.gler.assignment.dto.ForecastRequest;
import com.gler.assignment.dto.ForecastResponse;
import com.gler.assignment.dto.OpenMeteoResponse;
import com.gler.assignment.models.ForecastEntity;
import com.gler.assignment.exception.UpstreamApiException;
import com.gler.assignment.repositories.ForecastRepository;
import com.gler.assignment.services.ForecastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ForecastServiceImpl implements ForecastService {

    private static final String OPEN_METEO_API_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ForecastRepository forecastRepository;

    @Override
    public ForecastResponse processForecast(ForecastRequest request) {
        log.info("Processing forecast request: {}", request);

        // Call external API
        OpenMeteoResponse apiResponse = callOpenMeteoApi();

        // Extract maximum values based on request
        Double maxTemperature = null;
        Double maxHumidity = null;
        Double maxWindSpeed = null;

        if (request.getAddTemprature() && apiResponse.getHourly() != null
                && apiResponse.getHourly().getTemperature2m() != null) {
            maxTemperature = findMaxValue(apiResponse.getHourly().getTemperature2m());
        }

        if (request.getAddHumidity() && apiResponse.getHourly() != null
                && apiResponse.getHourly().getRelativeHumidity2m() != null) {
            maxHumidity = findMaxValue(
                    apiResponse.getHourly().getRelativeHumidity2m().stream()
                            .map(Integer::doubleValue)
                            .toList()
            );
        }

        if (request.getAddWindSpeed() && apiResponse.getHourly() != null
                && apiResponse.getHourly().getWindSpeed10m() != null) {
            maxWindSpeed = findMaxValue(apiResponse.getHourly().getWindSpeed10m());
        }

        // Store in database
        LocalDate today = LocalDate.now();
        ForecastEntity entity = forecastRepository.findByForecastDate(today)
                .orElse(ForecastEntity.builder()
                        .forecastDate(today)
                        .build());

        if (maxTemperature != null) {
            entity.setMaxTemperature(maxTemperature);
        }
        if (maxHumidity != null) {
            entity.setMaxHumidity(maxHumidity);
        }
        if (maxWindSpeed != null) {
            entity.setMaxWindSpeed(maxWindSpeed);
        }

        forecastRepository.save(entity);
        log.info("Forecast data saved for date: {}", today);

        // Build response
        return ForecastResponse.builder()
                .message("Forecast data processed successfully")
                .maxTemperature(maxTemperature)
                .maxHumidity(maxHumidity)
                .maxWindSpeed(maxWindSpeed)
                .date(today.toString())
                .build();
    }

    private OpenMeteoResponse callOpenMeteoApi() {
        try {
            log.info("Calling Open-Meteo API: {}", OPEN_METEO_API_URL);
            OpenMeteoResponse response = restTemplate.getForObject(
                    OPEN_METEO_API_URL,
                    OpenMeteoResponse.class
            );

            if (response == null) {
                throw new UpstreamApiException("Empty response from upstream API");
            }

            return response;
        } catch (RestClientException e) {
            log.error("Failed to call Open-Meteo API", e);
            throw new UpstreamApiException("Connection to the upstream is unreachable", e);
        }
    }

    private Double findMaxValue(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return Collections.max(values.stream()
                .filter(v -> v != null)
                .toList());
    }
}