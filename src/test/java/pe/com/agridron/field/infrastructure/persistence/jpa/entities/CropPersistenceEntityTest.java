package pe.com.agridron.field.infrastructure.persistence.jpa.entities;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;

import static org.assertj.core.api.Assertions.assertThat;

class CropPersistenceEntityTest extends AbstractFieldPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsCropWithAuditingFields() {
        var crop = new CropPersistenceEntity("Café", "Catimor");

        var persisted = entityManager.persistFlushFind(crop);

        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getName()).isEqualTo("Café");
        assertThat(persisted.getVariety()).isEqualTo("Catimor");
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getUpdatedAt()).isNotNull();
        assertThat(persisted.getVersion()).isZero();
    }
}
