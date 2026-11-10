package pe.com.agridron.field.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

/**
 * JPA representation of a crop in the catalog.
 */
@Entity
@Table(name = "crop")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CropPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String variety;

    public CropPersistenceEntity(String name, String variety) {
        this.name = name;
        this.variety = variety;
    }
}
