package pe.com.agridron.flight.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.com.agridron.flight.domain.model.valueobjects.AgrochemicalId;

/**
 * Converts {@link AgrochemicalId} values between the domain model and a {@code BIGINT} column.
 */
@Converter(autoApply = false)
public class AgrochemicalIdPersistenceConverter implements AttributeConverter<AgrochemicalId, Long> {

    @Override
    public Long convertToDatabaseColumn(AgrochemicalId attribute) {
        return attribute == null ? null : attribute.agrochemicalId();
    }

    @Override
    public AgrochemicalId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new AgrochemicalId(dbData);
    }
}
