package pe.com.agridron.analytics.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddOperationalMetricResource(
        @NotBlank @Size(max = 120) String name,
        @NotNull BigDecimal value,
        @NotBlank @Size(max = 30) String unit) {
}
