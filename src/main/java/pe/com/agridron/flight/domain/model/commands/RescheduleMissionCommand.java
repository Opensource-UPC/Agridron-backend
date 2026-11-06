package pe.com.agridron.flight.domain.model.commands;

import java.time.LocalDate;

/**
 * Command that moves a planned or authorized mission to another date.
 */
public record RescheduleMissionCommand(Long missionId, LocalDate requestedDate) {
}
