package com.gler.assignment.controllers;

import com.gler.assignment.dto.ForecastRequest;
import com.gler.assignment.dto.ForecastResponse;
import com.gler.assignment.services.ForecastService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ForcastController {

    @Autowired
    private ForecastService forecastService;

    @PostMapping("/forcast")
    public ResponseEntity<ForecastResponse> getForecast(@Valid @RequestBody ForecastRequest request) {
        ForecastResponse response = forecastService.processForecast(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}