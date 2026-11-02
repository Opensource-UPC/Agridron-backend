package pe.com.agridron.field.infrastructure.ports;

import org.springframework.stereotype.Component;
import pe.com.agridron.field.domain.ports.ParcelUsagePort;

/**
 * Temporary adapter that reports every parcel as free of active missions.
 *
 * <p>Flight Operations will replace this adapter with one backed by its mission
 * repository. Keeping the adapter here lets Field enforce the deactivation rule from day
 * one without importing another bounded context.</p>
 */
@Component
public class NoActiveMissionsParcelUsageAdapter implements ParcelUsagePort {

    @Override
    public boolean hasActiveMissions(Long parcelId) {
        return false;
    }
}
