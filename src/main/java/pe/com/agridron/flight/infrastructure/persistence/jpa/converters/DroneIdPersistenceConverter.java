package pe.com.agridron.flight.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.com.agridron.flight.domain.model.valueobjects.DroneId;

/**
 * Converts {@link DroneId} values between the domain model and a {@code BIGINT} column.
 */
@Converter(autoApply = false)
public class DroneIdPersistenceConverter implements AttributeConverter<DroneId, Long> {

    @Override
    public Long convertToDatabaseColumn(DroneId attribute) {
        return attribute == null ? null : attribute.droneId();
    }

    @Override
    public DroneId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new DroneId(dbData);
    }
}
