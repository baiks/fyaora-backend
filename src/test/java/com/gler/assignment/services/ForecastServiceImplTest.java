package com.gler.assignment.services;

import com.gler.assignment.dto.ForecastRequest;
import com.gler.assignment.dto.ForecastResponse;
import com.gler.assignment.dto.OpenMeteoResponse;
import com.gler.assignment.models.ForecastEntity;
import com.gler.assignment.exception.UpstreamApiException;
import com.gler.assignment.impl.ForecastServiceImpl;
import com.gler.assignment.repositories.ForecastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForecastServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ForecastRepository forecastRepository;

    @InjectMocks
    private ForecastServiceImpl forecastService;

    private static final String API_URL = 
        "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

    private OpenMeteoResponse mockApiResponse;
    private ForecastRequest request;

    @BeforeEach
    void setUp() {
        request = new ForecastRequest(true, true, true);
        
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
    void testProcessForecast_Success_AllParametersTrue() {
        // Arrange
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Forecast data processed successfully");
        assertThat(response.getMaxTemperature()).isEqualTo(15.8);
        assertThat(response.getMaxHumidity()).isEqualTo(87.0);
        assertThat(response.getMaxWindSpeed()).isEqualTo(12.5);
        assertThat(response.getDate()).isEqualTo(LocalDate.now().toString());

        verify(restTemplate, times(1)).getForObject(eq(API_URL), eq(OpenMeteoResponse.class));
        verify(forecastRepository, times(1)).findByForecastDate(any(LocalDate.class));
        verify(forecastRepository, times(1)).save(any(ForecastEntity.class));
    }

    @Test
    void testProcessForecast_OnlyTemperatureTrue() {
        // Arrange
        ForecastRequest tempOnlyRequest = new ForecastRequest(true, false, false);
        
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(tempOnlyRequest);

        // Assert
        assertThat(response.getMaxTemperature()).isEqualTo(15.8);
        assertThat(response.getMaxHumidity()).isNull();
        assertThat(response.getMaxWindSpeed()).isNull();

        ArgumentCaptor<ForecastEntity> entityCaptor = ArgumentCaptor.forClass(ForecastEntity.class);
        verify(forecastRepository).save(entityCaptor.capture());
        
        ForecastEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getMaxTemperature()).isEqualTo(15.8);
        assertThat(savedEntity.getMaxHumidity()).isNull();
        assertThat(savedEntity.getMaxWindSpeed()).isNull();
    }

    @Test
    void testProcessForecast_OnlyHumidityTrue() {
        // Arrange
        ForecastRequest humidityOnlyRequest = new ForecastRequest(false, true, false);
        
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(humidityOnlyRequest);

        // Assert
        assertThat(response.getMaxTemperature()).isNull();
        assertThat(response.getMaxHumidity()).isEqualTo(87.0);
        assertThat(response.getMaxWindSpeed()).isNull();
    }

    @Test
    void testProcessForecast_OnlyWindSpeedTrue() {
        // Arrange
        ForecastRequest windOnlyRequest = new ForecastRequest(false, false, true);
        
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(windOnlyRequest);

        // Assert
        assertThat(response.getMaxTemperature()).isNull();
        assertThat(response.getMaxHumidity()).isNull();
        assertThat(response.getMaxWindSpeed()).isEqualTo(12.5);
    }

    @Test
    void testProcessForecast_AllParametersFalse() {
        // Arrange
        ForecastRequest allFalseRequest = new ForecastRequest(false, false, false);
        
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(allFalseRequest);

        // Assert
        assertThat(response.getMaxTemperature()).isNull();
        assertThat(response.getMaxHumidity()).isNull();
        assertThat(response.getMaxWindSpeed()).isNull();
    }

    @Test
    void testProcessForecast_UpdatesExistingRecord() {
        // Arrange
        ForecastEntity existingEntity = ForecastEntity.builder()
                .id(1L)
                .forecastDate(LocalDate.now())
                .maxTemperature(10.0)
                .maxHumidity(70.0)
                .maxWindSpeed(8.0)
                .build();

        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(mockApiResponse);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.of(existingEntity));
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(request);

        // Assert
        assertThat(response.getMaxTemperature()).isEqualTo(15.8);
        assertThat(response.getMaxHumidity()).isEqualTo(87.0);
        assertThat(response.getMaxWindSpeed()).isEqualTo(12.5);

        ArgumentCaptor<ForecastEntity> entityCaptor = ArgumentCaptor.forClass(ForecastEntity.class);
        verify(forecastRepository).save(entityCaptor.capture());
        
        ForecastEntity updatedEntity = entityCaptor.getValue();
        assertThat(updatedEntity.getId()).isEqualTo(1L);
        assertThat(updatedEntity.getMaxTemperature()).isEqualTo(15.8);
        assertThat(updatedEntity.getMaxHumidity()).isEqualTo(87.0);
        assertThat(updatedEntity.getMaxWindSpeed()).isEqualTo(12.5);
    }

    @Test
    void testProcessForecast_ApiReturnsNull_ThrowsUpstreamApiException() {
        // Arrange
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> forecastService.processForecast(request))
                .isInstanceOf(UpstreamApiException.class)
                .hasMessage("Empty response from upstream API");

        verify(forecastRepository, never()).save(any(ForecastEntity.class));
    }

    @Test
    void testProcessForecast_ApiThrowsRestClientException_ThrowsUpstreamApiException() {
        // Arrange
        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        // Act & Assert
        assertThatThrownBy(() -> forecastService.processForecast(request))
                .isInstanceOf(UpstreamApiException.class)
                .hasMessage("Connection to the upstream is unreachable")
                .hasCauseInstanceOf(RestClientException.class);

        verify(forecastRepository, never()).save(any(ForecastEntity.class));
    }

    @Test
    void testProcessForecast_EmptyHourlyData_ReturnsNullValues() {
        // Arrange
        OpenMeteoResponse emptyResponse = new OpenMeteoResponse();
        OpenMeteoResponse.Hourly emptyHourly = new OpenMeteoResponse.Hourly();
        emptyHourly.setTemperature2m(Arrays.asList());
        emptyHourly.setRelativeHumidity2m(Arrays.asList());
        emptyHourly.setWindSpeed10m(Arrays.asList());
        emptyResponse.setHourly(emptyHourly);

        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(emptyResponse);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(request);

        // Assert
        assertThat(response.getMaxTemperature()).isNull();
        assertThat(response.getMaxHumidity()).isNull();
        assertThat(response.getMaxWindSpeed()).isNull();
    }

    @Test
    void testProcessForecast_NullHourlyData_ReturnsNullValues() {
        // Arrange
        OpenMeteoResponse nullHourlyResponse = new OpenMeteoResponse();
        nullHourlyResponse.setHourly(null);

        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(nullHourlyResponse);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(request);

        // Assert
        assertThat(response.getMaxTemperature()).isNull();
        assertThat(response.getMaxHumidity()).isNull();
        assertThat(response.getMaxWindSpeed()).isNull();
    }

    @Test
    void testProcessForecast_WithNullValuesInList() {
        // Arrange
        OpenMeteoResponse responseWithNulls = new OpenMeteoResponse();
        OpenMeteoResponse.Hourly hourlyWithNulls = new OpenMeteoResponse.Hourly();
        hourlyWithNulls.setTemperature2m(Arrays.asList(10.0, null, 15.8, null));
        hourlyWithNulls.setRelativeHumidity2m(Arrays.asList(70, null, 87, 80));
        hourlyWithNulls.setWindSpeed10m(Arrays.asList(null, 10.0, 12.5, null));
        responseWithNulls.setHourly(hourlyWithNulls);

        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(responseWithNulls);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(request);

        // Assert
        assertThat(response.getMaxTemperature()).isEqualTo(15.8);
        assertThat(response.getMaxHumidity()).isEqualTo(87.0);
        assertThat(response.getMaxWindSpeed()).isEqualTo(12.5);
    }

    @Test
    void testProcessForecast_SingleValueInList() {
        // Arrange
        OpenMeteoResponse singleValueResponse = new OpenMeteoResponse();
        OpenMeteoResponse.Hourly singleHourly = new OpenMeteoResponse.Hourly();
        singleHourly.setTemperature2m(Arrays.asList(20.5));
        singleHourly.setRelativeHumidity2m(Arrays.asList(65));
        singleHourly.setWindSpeed10m(Arrays.asList(5.0));
        singleValueResponse.setHourly(singleHourly);

        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(singleValueResponse);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(request);

        // Assert
        assertThat(response.getMaxTemperature()).isEqualTo(20.5);
        assertThat(response.getMaxHumidity()).isEqualTo(65.0);
        assertThat(response.getMaxWindSpeed()).isEqualTo(5.0);
    }

    @Test
    void testProcessForecast_NegativeTemperatures() {
        // Arrange
        OpenMeteoResponse negativeResponse = new OpenMeteoResponse();
        OpenMeteoResponse.Hourly negativeHourly = new OpenMeteoResponse.Hourly();
        negativeHourly.setTemperature2m(Arrays.asList(-5.0, -2.0, 0.0, -8.0));
        negativeHourly.setRelativeHumidity2m(Arrays.asList(90, 85, 95, 88));
        negativeHourly.setWindSpeed10m(Arrays.asList(15.0, 20.0, 18.0, 22.0));
        negativeResponse.setHourly(negativeHourly);

        when(restTemplate.getForObject(eq(API_URL), eq(OpenMeteoResponse.class)))
                .thenReturn(negativeResponse);
        when(forecastRepository.findByForecastDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(forecastRepository.save(any(ForecastEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ForecastResponse response = forecastService.processForecast(request);

        // Assert
        assertThat(response.getMaxTemperature()).isEqualTo(0.0);
        assertThat(response.getMaxHumidity()).isEqualTo(95.0);
        assertThat(response.getMaxWindSpeed()).isEqualTo(22.0);
    }
}