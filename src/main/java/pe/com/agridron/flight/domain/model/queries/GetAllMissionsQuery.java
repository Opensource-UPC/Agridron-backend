package pe.com.agridron.flight.domain.model.queries;

import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;

/**
 * Query that lists missions, optionally filtered by parcel and status ({@code null} means no filter).
 */
public record GetAllMissionsQuery(Long parcelId, MissionStatus status) {
}
