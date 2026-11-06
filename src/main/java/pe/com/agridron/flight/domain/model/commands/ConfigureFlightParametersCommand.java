package pe.com.agridron.flight.domain.model.commands;

/**
 * Command that configures the flight parameters of a planned mission.
 */
public record ConfigureFlightParametersCommand(Long missionId, Double flightHeight, Double flightSpeed, Double sprayRate, Double overlap) {
}
