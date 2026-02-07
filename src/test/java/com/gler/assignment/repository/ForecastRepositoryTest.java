package com.gler.assignment.repository;

import com.gler.assignment.models.ForecastEntity;
import com.gler.assignment.repositories.ForecastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ForecastRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ForecastRepository forecastRepository;

    private ForecastEntity testEntity;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        forecastRepository.deleteAll();
        
        testEntity = ForecastEntity.builder()
                .forecastDate(LocalDate.now())
                .maxTemperature(15.8)
                .maxHumidity(87.0)
                .maxWindSpeed(12.5)
                .build();
    }

    @Test
    void testSave_Success() {
        // Act
        ForecastEntity savedEntity = forecastRepository.save(testEntity);

        // Assert
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getId()).isNotNull();
        assertThat(savedEntity.getForecastDate()).isEqualTo(LocalDate.now());
        assertThat(savedEntity.getMaxTemperature()).isEqualTo(15.8);
        assertThat(savedEntity.getMaxHumidity()).isEqualTo(87.0);
        assertThat(savedEntity.getMaxWindSpeed()).isEqualTo(12.5);
        assertThat(savedEntity.getCreatedAt()).isNotNull();
    }

    @Test
    void testFindByForecastDate_Found() {
        // Arrange
        entityManager.persist(testEntity);
        entityManager.flush();

        // Act
        Optional<ForecastEntity> result = forecastRepository.findByForecastDate(LocalDate.now());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getForecastDate()).isEqualTo(LocalDate.now());
        assertThat(result.get().getMaxTemperature()).isEqualTo(15.8);
        assertThat(result.get().getMaxHumidity()).isEqualTo(87.0);
        assertThat(result.get().getMaxWindSpeed()).isEqualTo(12.5);
    }

    @Test
    void testFindByForecastDate_NotFound() {
        // Act
        Optional<ForecastEntity> result = forecastRepository.findByForecastDate(LocalDate.now().minusDays(1));

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testFindByForecastDate_MultipleDates() {
        // Arrange
        ForecastEntity yesterday = ForecastEntity.builder()
                .forecastDate(LocalDate.now().minusDays(1))
                .maxTemperature(10.0)
                .maxHumidity(75.0)
                .maxWindSpeed(8.0)
                .build();

        ForecastEntity tomorrow = ForecastEntity.builder()
                .forecastDate(LocalDate.now().plusDays(1))
                .maxTemperature(20.0)
                .maxHumidity(65.0)
                .maxWindSpeed(15.0)
                .build();

        entityManager.persist(testEntity);
        entityManager.persist(yesterday);
        entityManager.persist(tomorrow);
        entityManager.flush();

        // Act
        Optional<ForecastEntity> todayResult = forecastRepository.findByForecastDate(LocalDate.now());
        Optional<ForecastEntity> yesterdayResult = forecastRepository.findByForecastDate(LocalDate.now().minusDays(1));
        Optional<ForecastEntity> tomorrowResult = forecastRepository.findByForecastDate(LocalDate.now().plusDays(1));

        // Assert
        assertThat(todayResult).isPresent();
        assertThat(todayResult.get().getMaxTemperature()).isEqualTo(15.8);

        assertThat(yesterdayResult).isPresent();
        assertThat(yesterdayResult.get().getMaxTemperature()).isEqualTo(10.0);

        assertThat(tomorrowResult).isPresent();
        assertThat(tomorrowResult.get().getMaxTemperature()).isEqualTo(20.0);
    }

    @Test
    void testSave_WithNullValues() {
        // Arrange
        ForecastEntity entityWithNulls = ForecastEntity.builder()
                .forecastDate(LocalDate.now())
                .maxTemperature(null)
                .maxHumidity(null)
                .maxWindSpeed(null)
                .build();

        // Act
        ForecastEntity savedEntity = forecastRepository.save(entityWithNulls);

        // Assert
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getId()).isNotNull();
        assertThat(savedEntity.getMaxTemperature()).isNull();
        assertThat(savedEntity.getMaxHumidity()).isNull();
        assertThat(savedEntity.getMaxWindSpeed()).isNull();
    }

    @Test
    void testUpdate_ExistingEntity() {
        // Arrange
        ForecastEntity savedEntity = entityManager.persist(testEntity);
        entityManager.flush();

        // Act - Update values
        savedEntity.setMaxTemperature(20.0);
        savedEntity.setMaxHumidity(90.0);
        savedEntity.setMaxWindSpeed(18.0);
        
        ForecastEntity updatedEntity = forecastRepository.save(savedEntity);
        entityManager.flush();

        // Assert
        Optional<ForecastEntity> retrievedEntity = forecastRepository.findByForecastDate(LocalDate.now());
        assertThat(retrievedEntity).isPresent();
        assertThat(retrievedEntity.get().getId()).isEqualTo(savedEntity.getId());
        assertThat(retrievedEntity.get().getMaxTemperature()).isEqualTo(20.0);
        assertThat(retrievedEntity.get().getMaxHumidity()).isEqualTo(90.0);
        assertThat(retrievedEntity.get().getMaxWindSpeed()).isEqualTo(18.0);
    }

    @Test
    void testFindAll_MultipleRecords() {
        // Arrange
        ForecastEntity entity1 = ForecastEntity.builder()
                .forecastDate(LocalDate.now().minusDays(2))
                .maxTemperature(12.0)
                .maxHumidity(80.0)
                .maxWindSpeed(10.0)
                .build();

        ForecastEntity entity2 = ForecastEntity.builder()
                .forecastDate(LocalDate.now().minusDays(1))
                .maxTemperature(14.0)
                .maxHumidity(85.0)
                .maxWindSpeed(11.0)
                .build();

        entityManager.persist(entity1);
        entityManager.persist(entity2);
        entityManager.persist(testEntity);
        entityManager.flush();

        // Act
        List<ForecastEntity> allEntities = forecastRepository.findAll();

        // Assert
        assertThat(allEntities).hasSize(3);
        assertThat(allEntities).extracting(ForecastEntity::getForecastDate)
                .containsExactlyInAnyOrder(
                        LocalDate.now().minusDays(2),
                        LocalDate.now().minusDays(1),
                        LocalDate.now()
                );
    }

    @Test
    void testDeleteByForecastDate() {
        // Arrange
        ForecastEntity savedEntity = entityManager.persist(testEntity);
        entityManager.flush();
        Long savedId = savedEntity.getId();

        // Act
        forecastRepository.deleteById(savedId);
        entityManager.flush();

        // Assert
        Optional<ForecastEntity> deletedEntity = forecastRepository.findByForecastDate(LocalDate.now());
        assertThat(deletedEntity).isEmpty();
    }

    @Test
    void testCreatedAtTimestamp_AutoGenerated() {
        // Act
        ForecastEntity savedEntity = forecastRepository.save(testEntity);
        entityManager.flush();

        // Assert
        assertThat(savedEntity.getCreatedAt()).isNotNull();
        assertThat(savedEntity.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(savedEntity.getCreatedAt()).isAfter(LocalDateTime.now().minusSeconds(5));
    }

    @Test
    void testSave_WithPartialData() {
        // Arrange - Only temperature
        ForecastEntity tempOnly = ForecastEntity.builder()
                .forecastDate(LocalDate.now())
                .maxTemperature(18.5)
                .maxHumidity(null)
                .maxWindSpeed(null)
                .build();

        // Act
        ForecastEntity savedEntity = forecastRepository.save(tempOnly);

        // Assert
        assertThat(savedEntity.getMaxTemperature()).isEqualTo(18.5);
        assertThat(savedEntity.getMaxHumidity()).isNull();
        assertThat(savedEntity.getMaxWindSpeed()).isNull();
    }

    @Test
    void testFindByForecastDate_WithNegativeTemperature() {
        // Arrange
        ForecastEntity negativeTemp = ForecastEntity.builder()
                .forecastDate(LocalDate.now())
                .maxTemperature(-5.0)
                .maxHumidity(95.0)
                .maxWindSpeed(25.0)
                .build();

        entityManager.persist(negativeTemp);
        entityManager.flush();

        // Act
        Optional<ForecastEntity> result = forecastRepository.findByForecastDate(LocalDate.now());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getMaxTemperature()).isEqualTo(-5.0);
    }

    @Test
    void testFindByForecastDate_WithZeroValues() {
        // Arrange
        ForecastEntity zeroValues = ForecastEntity.builder()
                .forecastDate(LocalDate.now())
                .maxTemperature(0.0)
                .maxHumidity(0.0)
                .maxWindSpeed(0.0)
                .build();

        entityManager.persist(zeroValues);
        entityManager.flush();

        // Act
        Optional<ForecastEntity> result = forecastRepository.findByForecastDate(LocalDate.now());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getMaxTemperature()).isEqualTo(0.0);
        assertThat(result.get().getMaxHumidity()).isEqualTo(0.0);
        assertThat(result.get().getMaxWindSpeed()).isEqualTo(0.0);
    }

    @Test
    void testCount_AfterInserts() {
        // Arrange
        entityManager.persist(testEntity);
        entityManager.persist(ForecastEntity.builder()
                .forecastDate(LocalDate.now().minusDays(1))
                .maxTemperature(10.0)
                .build());
        entityManager.flush();

        // Act
        long count = forecastRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testExistsById() {
        // Arrange
        ForecastEntity savedEntity = entityManager.persist(testEntity);
        entityManager.flush();

        // Act
        boolean exists = forecastRepository.existsById(savedEntity.getId());
        boolean notExists = forecastRepository.existsById(999L);

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}