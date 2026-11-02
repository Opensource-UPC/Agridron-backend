package pe.com.agridron.analytics.domain.model.valueobjects;

/**
 * Performance indicators calculated for a period, with their display name and unit.
 */
public enum IndicatorMetric {
    HECTARES_TREATED("Hectares treated", "ha"),
    COMPLETION_RATE("Completion rate", "%"),
    APPLIED_VOLUME("Applied volume", "L"),
    APPLICATION_RATE("Application rate", "L/ha");

    private final String displayName;
    private final String unit;

    IndicatorMetric(String displayName, String unit) {
        this.displayName = displayName;
        this.unit = unit;
    }

    public String displayName() {
        return displayName;
    }

    public String unit() {
        return unit;
    }
}
