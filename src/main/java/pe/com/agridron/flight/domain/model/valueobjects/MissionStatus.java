package pe.com.agridron.flight.domain.model.valueobjects;

/**
 * Lifecycle status of a mission.
 *
 * <pre>
 * PLANNED ──authorize──▶ AUTHORIZED ──start──▶ IN_PROGRESS ──complete──▶ COMPLETED
 *                                               │      ▲
 *                                             pause  resume
 *                                               ▼      │
 *                                               PAUSED
 * Any non-terminal status ──cancel──▶ CANCELLED
 * </pre>
 */
public enum MissionStatus {
    /** The mission is being planned; resources, operator and parameters can change. */
    PLANNED,
    /** The mission has every resource assigned and is authorized to fly. */
    AUTHORIZED,
    /** The drone is flying the mission. */
    IN_PROGRESS,
    /** The flight is temporarily paused. */
    PAUSED,
    /** The mission finished and its result was registered. */
    COMPLETED,
    /** The mission was cancelled. */
    CANCELLED;

    /**
     * Returns {@code true} when the mission can no longer change.
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * Returns {@code true} when the mission still holds its parcel and resources.
     */
    public boolean isActive() {
        return !isTerminal();
    }
}
