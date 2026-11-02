package pe.com.agridron.field.domain.ports;

/**
 * Outbound port that answers whether a parcel is currently used by an active mission.
 *
 * <p>Field needs this information to enforce the deactivation rule (FM-006) without
 * depending on the Flight Operations domain. The real adapter belongs to Flight
 * Operations; until that bounded context exists, Field uses a permissive adapter that
 * always reports no active missions.</p>
 */
public interface ParcelUsagePort {

    /**
     * Checks whether the parcel participates in at least one active mission.
     *
     * @param parcelId identifier of the parcel
     * @return {@code true} when the parcel is in use
     */
    boolean hasActiveMissions(Long parcelId);
}
