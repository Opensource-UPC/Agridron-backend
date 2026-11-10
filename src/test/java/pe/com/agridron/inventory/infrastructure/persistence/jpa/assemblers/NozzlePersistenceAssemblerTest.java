package pe.com.agridron.inventory.infrastructure.persistence.jpa.assemblers;

import org.junit.jupiter.api.Test;
import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleStock;
import pe.com.agridron.inventory.infrastructure.persistence.jpa.entities.NozzlePersistenceEntity;

import static org.assertj.core.api.Assertions.assertThat;

class NozzlePersistenceAssemblerTest {

    @Test
    void mapsANewNozzleToARowWithoutId() {
        var nozzle = Nozzle.register("flat-fan", 5.0, new NozzleStock(6, 2, 1));

        var entity = NozzlePersistenceAssembler.toPersistenceFromDomain(nozzle);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getType()).isEqualTo("flat-fan");
        assertThat(entity.getDiameterMm()).isEqualTo(5.0);
        assertThat(entity.getGoodUnits()).isEqualTo(6);
        assertThat(entity.getWornUnits()).isEqualTo(2);
        assertThat(entity.getDamagedUnits()).isEqualTo(1);
    }

    @Test
    void mapsARowBackToTheAggregate() {
        var entity = new NozzlePersistenceEntity("hollow-cone", 3.0, 9, 3, 2);
        entity.setId(2L);

        var nozzle = NozzlePersistenceAssembler.toDomainFromPersistence(entity);

        assertThat(nozzle.getId()).isEqualTo(2L);
        assertThat(nozzle.getType()).isEqualTo("hollow-cone");
        assertThat(nozzle.getStock()).isEqualTo(new NozzleStock(9, 3, 2));
    }

    @Test
    void copiesTheUpdatedStockToAnExistingRow() {
        var entity = new NozzlePersistenceEntity("flat-fan", 5.0, 6, 0, 0);
        entity.setId(1L);
        var nozzle = NozzlePersistenceAssembler.toDomainFromPersistence(entity);
        nozzle.takeOneUnit();

        NozzlePersistenceAssembler.copyToPersistence(nozzle, entity);

        assertThat(entity.getGoodUnits()).isEqualTo(5);
    }
}