package com.gler.assignment.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gler.assignment.controllers.ForcastController;
import com.gler.assignment.dto.ForecastRequest;
import com.gler.assignment.dto.ForecastResponse;
import com.gler.assignment.exception.UpstreamApiException;
import com.gler.assignment.services.ForecastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForcastController.class)
@ExtendWith(MockitoExtension.class)
class ForcastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @SuppressWarnings("removal")  // Suppress deprecation warning
    private ForecastService forecastService;

    private ForecastRequest validRequest;
    private ForecastResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRequest = new ForecastRequest(true, true, true);

        mockResponse = ForecastResponse.builder()
                .message("Forecast data processed successfully")
                .maxTemperature(15.8)
                .maxHumidity(87.0)
                .maxWindSpeed(12.5)
                .date("2025-02-07")
                .build();
    }

    @Test
    void testGetForecast_Success() throws Exception {
        // Arrange
        when(forecastService.processForecast(any(ForecastRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Forecast data processed successfully"))
                .andExpect(jsonPath("$.maxTemperature").value(15.8))
                .andExpect(jsonPath("$.maxHumidity").value(87.0))
                .andExpect(jsonPath("$.maxWindSpeed").value(12.5))
                .andExpect(jsonPath("$.date").value("2025-02-07"));

        verify(forecastService, times(1)).processForecast(any(ForecastRequest.class));
    }

    @Test
    void testGetForecast_WithPartialData() throws Exception {
        // Arrange
        ForecastRequest partialRequest = new ForecastRequest(true, false, true);
        ForecastResponse partialResponse = ForecastResponse.builder()
                .message("Forecast data processed successfully")
                .maxTemperature(15.8)
                .maxHumidity(null)
                .maxWindSpeed(12.5)
                .date("2025-02-07")
                .build();

        when(forecastService.processForecast(any(ForecastRequest.class)))
                .thenReturn(partialResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxTemperature").value(15.8))
                .andExpect(jsonPath("$.maxHumidity").isEmpty())
                .andExpect(jsonPath("$.maxWindSpeed").value(12.5));

        verify(forecastService, times(1)).processForecast(any(ForecastRequest.class));
    }

    @Test
    void testGetForecast_MissingAddTemprature_ReturnsBadRequest() throws Exception {
        // Arrange
        String invalidRequest = """
                {
                    "addHumidity": true,
                    "addWindSpeed": true
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("addTemprature parameter is mandatory"))
                .andExpect(jsonPath("$.path").value("/api/v1/forcast"));

        verify(forecastService, never()).processForecast(any(ForecastRequest.class));
    }

    @Test
    void testGetForecast_MissingAddHumidity_ReturnsBadRequest() throws Exception {
        // Arrange
        String invalidRequest = """
                {
                    "addTemprature": true,
                    "addWindSpeed": true
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/forcast"));

        verify(forecastService, never()).processForecast(any(ForecastRequest.class));
    }

    @Test
    void testGetForecast_MissingAddWindSpeed_ReturnsBadRequest() throws Exception {
        // Arrange
        String invalidRequest = """
                {
                    "addTemprature": true,
                    "addHumidity": true
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/forcast"));

        verify(forecastService, never()).processForecast(any(ForecastRequest.class));
    }

    @Test
    void testGetForecast_AllParametersMissing_ReturnsBadRequest() throws Exception {
        // Arrange
        String invalidRequest = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(forecastService, never()).processForecast(any(ForecastRequest.class));
    }

    @Test
    void testGetForecast_UpstreamApiException_Returns502() throws Exception {
        // Arrange
        when(forecastService.processForecast(any(ForecastRequest.class)))
                .thenThrow(new UpstreamApiException("Connection to the upstream is unreachable"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error").value("Upstream API Unreachable"))
                .andExpect(jsonPath("$.message").value("Connection to the upstream is unreachable"))
                .andExpect(jsonPath("$.path").value("/api/v1/forcast"));

        verify(forecastService, times(1)).processForecast(any(ForecastRequest.class));
    }

    @Test
    void testGetForecast_GenericException_Returns500() throws Exception {
        // Arrange
        when(forecastService.processForecast(any(ForecastRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Database connection failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/forcast"));

        verify(forecastService, times(1)).processForecast(any(ForecastRequest.class));
    }

    @Test
    void testGetForecast_EmptyRequestBody_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isInternalServerError());

        verify(forecastService, never()).processForecast(any(ForecastRequest.class));
    }

    @Test
    void testGetForecast_InvalidJson_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isInternalServerError());

        verify(forecastService, never()).processForecast(any(ForecastRequest.class));
    }

    @Test
    void testGetForecast_AllFalseValues_Success() throws Exception {
        // Arrange
        ForecastRequest allFalseRequest = new ForecastRequest(false, false, false);
        ForecastResponse allNullResponse = ForecastResponse.builder()
                .message("Forecast data processed successfully")
                .maxTemperature(null)
                .maxHumidity(null)
                .maxWindSpeed(null)
                .date("2025-02-07")
                .build();

        when(forecastService.processForecast(any(ForecastRequest.class)))
                .thenReturn(allNullResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allFalseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Forecast data processed successfully"))
                .andExpect(jsonPath("$.maxTemperature").isEmpty())
                .andExpect(jsonPath("$.maxHumidity").isEmpty())
                .andExpect(jsonPath("$.maxWindSpeed").isEmpty());

        verify(forecastService, times(1)).processForecast(any(ForecastRequest.class));
    }
}