package pe.com.agridron.analytics.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.entities.PerformanceIndicatorPersistenceEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceIndicatorPersistenceRepository
        extends JpaRepository<PerformanceIndicatorPersistenceEntity, Long> {

    Optional<PerformanceIndicatorPersistenceEntity> findByNameAndPeriod(String name, String period);

    List<PerformanceIndicatorPersistenceEntity> findAllByPeriodOrderByNameAsc(String period);
}
