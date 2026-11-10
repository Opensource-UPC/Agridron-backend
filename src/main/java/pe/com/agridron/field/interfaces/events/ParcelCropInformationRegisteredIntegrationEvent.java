package pe.com.agridron.field.interfaces.events;

import pe.com.agridron.field.domain.events.ParcelCropInformationRegisteredEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published when the crop information of a parcel is registered.
 *
 * <p>The phenological stage is exposed as its stable name so consumers do not depend on
 * the Field domain enum.</p>
 */
public record ParcelCropInformationRegisteredIntegrationEvent(
        String eventId,
        Instant occurredOn,
        Long parcelId,
        Long farmId,
        Long cropId,
        String phenologicalStage) implements IntegrationEvent {

    public static final String EVENT_TYPE = "field.parcel.crop-information-registered";

    /**
     * Translates the domain event into its integration event representation.
     */
    public static ParcelCropInformationRegisteredIntegrationEvent from(
            ParcelCropInformationRegisteredEvent event) {
        return new ParcelCropInformationRegisteredIntegrationEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                event.parcelId(),
                event.farmId(),
                event.cropId(),
                event.phenologicalStage() == null ? null : event.phenologicalStage().name());
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
