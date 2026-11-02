package pe.com.agridron.analytics.domain.repositories;

import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;

import java.util.List;
import java.util.Optional;

public interface PerformanceIndicatorRepository {

    PerformanceIndicator save(PerformanceIndicator indicator);

    Optional<PerformanceIndicator> findByNameAndPeriod(String name, String period);

    List<PerformanceIndicator> findAll();

    List<PerformanceIndicator> findByPeriod(String period);
}
