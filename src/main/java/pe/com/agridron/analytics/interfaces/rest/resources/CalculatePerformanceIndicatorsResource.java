package pe.com.agridron.analytics.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CalculatePerformanceIndicatorsResource(
        @NotBlank @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])") String period) {
}
