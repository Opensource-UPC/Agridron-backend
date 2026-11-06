
package pe.com.agridron.inventory.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleStock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NozzleTest {

    @Test
    void registersANozzleWithoutIdAndTrimsType() {
        var nozzle = Nozzle.register("  flat-fan  ", 5.0, new NozzleStock(6, 0, 0));

        assertThat(nozzle.getId()).isNull();
        assertThat(nozzle.getType()).isEqualTo("flat-fan");
        assertThat(nozzle.getDiameterMm()).isEqualTo(5.0);
        assertThat(nozzle.getStock()).isEqualTo(new NozzleStock(6, 0, 0));
    }

    @Test
    void registersWithEmptyStockWhenNoneIsGiven() {
        var nozzle = Nozzle.register("hollow-cone", 3.0, null);

        assertThat(nozzle.getStock()).isEqualTo(NozzleStock.empty());
        assertThat(nozzle.hasInstallableUnits()).isFalse();
    }

    @Test
    void rejectsBlankTypeAndNonPositiveDiameter() {
        assertThatThrownBy(() -> Nozzle.register(" ", 5.0, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Nozzle.register("flat-fan", 0.0, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Nozzle.register("flat-fan", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstituteKeepsTheId() {
        var nozzle = Nozzle.reconstitute(7L, "flat-fan", 5.0, new NozzleStock(1, 2, 3));

        assertThat(nozzle.getId()).isEqualTo(7L);
        assertThat(nozzle.getStock().total()).isEqualTo(6);
    }

    @Test
    void stockOperationsUpdateTheAggregate() {
        var nozzle = Nozzle.register("flat-fan", 5.0, new NozzleStock(5, 0, 0));

        nozzle.restock(2);
        nozzle.markAsWorn(1);
        nozzle.markAsDamaged(1);
        nozzle.takeOneUnit();

        assertThat(nozzle.getStock()).isEqualTo(new NozzleStock(4, 1, 1));
    }

    @Test
    void cannotTakeAUnitWhenOnlyDamagedUnitsRemain() {
        var nozzle = Nozzle.register("flat-fan", 5.0, new NozzleStock(0, 0, 3));

        assertThat(nozzle.hasInstallableUnits()).isFalse();

        assertThatThrownBy(nozzle::takeOneUnit).isInstanceOf(IllegalStateException.class);
    }
}