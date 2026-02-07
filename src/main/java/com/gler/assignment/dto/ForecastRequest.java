package com.gler.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForecastRequest {
    
    @NotNull(message = "addTemprature parameter is mandatory")
    private Boolean addTemprature;
    
    @NotNull(message = "addHumidity parameter is mandatory")
    private Boolean addHumidity;
    
    @NotNull(message = "addWindSpeed parameter is mandatory")
    private Boolean addWindSpeed;
}