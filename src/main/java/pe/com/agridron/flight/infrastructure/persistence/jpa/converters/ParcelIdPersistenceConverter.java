package pe.com.agridron.flight.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.com.agridron.flight.domain.model.valueobjects.ParcelId;

/**
 * Converts {@link ParcelId} values between the domain model and a {@code BIGINT} column.
 */
@Converter(autoApply = false)
public class ParcelIdPersistenceConverter implements AttributeConverter<ParcelId, Long> {

    @Override
    public Long convertToDatabaseColumn(ParcelId attribute) {
        return attribute == null ? null : attribute.parcelId();
    }

    @Override
    public ParcelId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new ParcelId(dbData);
    }
}
