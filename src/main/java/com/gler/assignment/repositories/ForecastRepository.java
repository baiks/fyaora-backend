package com.gler.assignment.repositories;

import com.gler.assignment.models.ForecastEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ForecastRepository extends JpaRepository<ForecastEntity, Long> {
    Optional<ForecastEntity> findByForecastDate(LocalDate date);
}