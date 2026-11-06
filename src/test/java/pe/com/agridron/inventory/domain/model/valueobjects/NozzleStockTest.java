package pe.com.agridron.inventory.domain.model.valueobjects;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NozzleStockTest {

    @Test
    void rejectsNegativeUnits() {
        assertThatThrownBy(() -> new NozzleStock(-1, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NozzleStock(0, -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NozzleStock(0, 0, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void totalAddsAllStates() {
        assertThat(new NozzleStock(6, 6, 3).total()).isEqualTo(15);
    }

    @Test
    void restockAddsGoodUnits() {
        var stock = new NozzleStock(2, 1, 0).restock(5);

        assertThat(stock).isEqualTo(new NozzleStock(7, 1, 0));
    }

    @Test
    void markAsWornMovesGoodUnitsToWorn() {
        var stock = new NozzleStock(5, 0, 0).markAsWorn(2);

        assertThat(stock).isEqualTo(new NozzleStock(3, 2, 0));
    }

    @Test
    void markAsDamagedMovesGoodUnitsToDamaged() {
        var stock = new NozzleStock(5, 0, 0).markAsDamaged(1);

        assertThat(stock).isEqualTo(new NozzleStock(4, 0, 1));
    }

    @Test
    void cannotMarkMoreUnitsThanAvailable() {
        var stock = new NozzleStock(1, 0, 0);

        assertThatThrownBy(() -> stock.markAsWorn(2)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> stock.markAsDamaged(2)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void operationsRequirePositiveUnits() {
        var stock = new NozzleStock(5, 0, 0);

        assertThatThrownBy(() -> stock.restock(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> stock.markAsWorn(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void takeOneRemovesOneGoodUnit() {
        assertThat(new NozzleStock(3, 1, 1).takeOne()).isEqualTo(new NozzleStock(2, 1, 1));
    }

    @Test
    void takeOneFailsWithoutGoodUnits() {
        var stock = new NozzleStock(0, 4, 2);

        assertThat(stock.hasInstallableUnits()).isFalse();
        assertThatThrownBy(stock::takeOne).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void operationsDoNotModifyTheOriginal() {
        var original = new NozzleStock(5, 0, 0);

        original.restock(3);

        assertThat(original).isEqualTo(new NozzleStock(5, 0, 0));
    }
}