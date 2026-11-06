package pe.com.agridron.flight.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.com.agridron.flight.domain.model.valueobjects.OperatorId;

/**
 * Converts {@link OperatorId} values between the domain model and a {@code BIGINT} column.
 */
@Converter(autoApply = false)
public class OperatorIdPersistenceConverter implements AttributeConverter<OperatorId, Long> {

    @Override
    public Long convertToDatabaseColumn(OperatorId attribute) {
        return attribute == null ? null : attribute.operatorId();
    }

    @Override
    public OperatorId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new OperatorId(dbData);
    }
}
