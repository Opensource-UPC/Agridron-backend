package pe.com.agridron.flight.domain.model.commands;

import java.time.LocalDate;

/**
 * Command that plans a new mission over a parcel.
 */
public record PlanMissionCommand(Long parcelId, LocalDate requestedDate) {
}
