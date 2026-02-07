package com.gler.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResponse {
    private String message;
    private Double maxTemperature;
    private Double maxHumidity;
    private Double maxWindSpeed;
    private String date;
}