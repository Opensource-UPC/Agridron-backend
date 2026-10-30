package pe.com.agridron.field.infrastructure.ports;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoActiveMissionsParcelUsageAdapterTest {

    private final NoActiveMissionsParcelUsageAdapter adapter =
            new NoActiveMissionsParcelUsageAdapter();

    @Test
    void reportsNoActiveMissionsForAnyParcel() {
        assertThat(adapter.hasActiveMissions(1L)).isFalse();
        assertThat(adapter.hasActiveMissions(999_999L)).isFalse();
    }
}
