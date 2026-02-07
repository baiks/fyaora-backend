package com.gler.assignment.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "forecast_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;
    
    @Column(name = "max_temperature")
    private Double maxTemperature;
    
    @Column(name = "max_humidity")
    private Double maxHumidity;
    
    @Column(name = "max_wind_speed")
    private Double maxWindSpeed;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}