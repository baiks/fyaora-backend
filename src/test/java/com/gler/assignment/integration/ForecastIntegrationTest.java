package com.gler.assignment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gler.assignment.dto.ForecastRequest;
import com.gler.assignment.dto.OpenMeteoResponse;
import com.gler.assignment.models.ForecastEntity;
import com.gler.assignment.repositories.ForecastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ForecastIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ForecastRepository forecastRepository;

    @MockBean
    @SuppressWarnings("removal")  // Suppress deprecation warning
    private RestTemplate restTemplate;

    private static final String API_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

    private OpenMeteoResponse mockApiResponse;

    @BeforeEach
    void setUp() {
        forecastRepository.deleteAll();

        // Create mock API response
        mockApiResponse = new OpenMeteoResponse();
        mockApiResponse.setLatitude(52.52);
        mockApiResponse.setLongitude(13.41);

        OpenMeteoResponse.Hourly hourly = new OpenMeteoResponse.Hourly();
        hourly.setTemperature2m(Arrays.asList(10.0, 12.0, 15.8, 14.0, 11.0));
        hourly.setRelativeHumidity2m(Arrays.asList(70, 75, 87, 80, 72));
        hourly.setWindSpeed10m(Arrays.asList(8.0, 10.0, 12.5, 9.0, 7.5));

        mockApiResponse.setHourly(hourly);
    }

    @Test
    void testEndToEndFlow_Success() throws Exception {
        // Arrange
        ForecastRequest request = new ForecastRequest(true, true, true);
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Forecast data processed successfully"))
                .andExpect(jsonPath("$.maxTemperature").value(15.8))
                .andExpect(jsonPath("$.maxHumidity").value(87.0))
                .andExpect(jsonPath("$.maxWindSpeed").value(12.5))
                .andExpect(jsonPath("$.date").exists());

        // Verify database
        Optional<ForecastEntity> savedEntity = forecastRepository.findByForecastDate(LocalDate.now());
        assertThat(savedEntity).isPresent();
        assertThat(savedEntity.get().getMaxTemperature()).isEqualTo(15.8);
        assertThat(savedEntity.get().getMaxHumidity()).isEqualTo(87.0);
        assertThat(savedEntity.get().getMaxWindSpeed()).isEqualTo(12.5);
    }

    @Test
    void testEndToEndFlow_PartialDataRequest() throws Exception {
        // Arrange
        ForecastRequest request = new ForecastRequest(true, false, true);
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxTemperature").value(15.8))
                .andExpect(jsonPath("$.maxHumidity").isEmpty())
                .andExpect(jsonPath("$.maxWindSpeed").value(12.5));

        // Verify database
        Optional<ForecastEntity> savedEntity = forecastRepository.findByForecastDate(LocalDate.now());
        assertThat(savedEntity).isPresent();
        assertThat(savedEntity.get().getMaxTemperature()).isEqualTo(15.8);
        assertThat(savedEntity.get().getMaxHumidity()).isNull();
        assertThat(savedEntity.get().getMaxWindSpeed()).isEqualTo(12.5);
    }

    @Test
    void testEndToEndFlow_UpdateExistingRecord() throws Exception {
        // Arrange - Create existing record
        ForecastEntity existingEntity = ForecastEntity.builder()
                .forecastDate(LocalDate.now())
                .maxTemperature(10.0)
                .maxHumidity(70.0)
                .maxWindSpeed(8.0)
                .build();
        forecastRepository.save(existingEntity);

        ForecastRequest request = new ForecastRequest(true, true, true);
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxTemperature").value(15.8))
                .andExpect(jsonPath("$.maxHumidity").value(87.0))
                .andExpect(jsonPath("$.maxWindSpeed").value(12.5));

        // Verify only one record exists with updated values
        long count = forecastRepository.count();
        assertThat(count).isEqualTo(1);

        Optional<ForecastEntity> updatedEntity = forecastRepository.findByForecastDate(LocalDate.now());
        assertThat(updatedEntity).isPresent();
        assertThat(updatedEntity.get().getMaxTemperature()).isEqualTo(15.8);
        assertThat(updatedEntity.get().getMaxHumidity()).isEqualTo(87.0);
        assertThat(updatedEntity.get().getMaxWindSpeed()).isEqualTo(12.5);
    }

    @Test
    void testEndToEndFlow_ValidationError() throws Exception {
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
                .andExpect(jsonPath("$.message").value("addWindSpeed parameter is mandatory"))
                .andExpect(jsonPath("$.path").value("/api/v1/forcast"))
                .andExpect(jsonPath("$.timestamp").exists());

        // Verify no database save
        long count = forecastRepository.count();
        assertThat(count).isZero();
    }

    @Test
    void testEndToEndFlow_UpstreamApiError() throws Exception {
        // Arrange
        ForecastRequest request = new ForecastRequest(true, true, true);
        when(restTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error").value("Upstream API Unreachable"))
                .andExpect(jsonPath("$.message").value("Connection to the upstream is unreachable"))
                .andExpect(jsonPath("$.path").value("/api/v1/forcast"))
                .andExpect(jsonPath("$.timestamp").exists());

        // Verify no database save
        long count = forecastRepository.count();
        assertThat(count).isZero();
    }

    @Test
    void testEndToEndFlow_AllParametersFalse() throws Exception {
        // Arrange
        ForecastRequest request = new ForecastRequest(false, false, false);
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxTemperature").isEmpty())
                .andExpect(jsonPath("$.maxHumidity").isEmpty())
                .andExpect(jsonPath("$.maxWindSpeed").isEmpty());

        // Verify database record with null values
        Optional<ForecastEntity> savedEntity = forecastRepository.findByForecastDate(LocalDate.now());
        assertThat(savedEntity).isPresent();
        assertThat(savedEntity.get().getMaxTemperature()).isNull();
        assertThat(savedEntity.get().getMaxHumidity()).isNull();
        assertThat(savedEntity.get().getMaxWindSpeed()).isNull();
    }

    @Test
    void testEndToEndFlow_MultipleRequests_SameDay() throws Exception {
        // Arrange
        ForecastRequest request1 = new ForecastRequest(true, false, false);
        ForecastRequest request2 = new ForecastRequest(false, true, false);
        ForecastRequest request3 = new ForecastRequest(false, false, true);

        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);

        // Act - First request (temperature only)
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Act - Second request (humidity only)
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        // Act - Third request (wind speed only)
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isOk());

        // Assert - Only one record should exist with all values
        long count = forecastRepository.count();
        assertThat(count).isEqualTo(1);

        Optional<ForecastEntity> finalEntity = forecastRepository.findByForecastDate(LocalDate.now());
        assertThat(finalEntity).isPresent();
        // Last request had only wind speed, so temperature and humidity should be null
        assertThat(finalEntity.get().getMaxTemperature()).isNull();
        assertThat(finalEntity.get().getMaxHumidity()).isNull();
        assertThat(finalEntity.get().getMaxWindSpeed()).isEqualTo(12.5);
    }

    @Test
    void testEndToEndFlow_ContentTypeValidation() throws Exception {
        // Arrange
        ForecastRequest request = new ForecastRequest(true, true, true);

        // Act & Assert - Missing Content-Type
        mockMvc.perform(post("/api/v1/forcast")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void testEndToEndFlow_EmptyRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isInternalServerError());
    }
}