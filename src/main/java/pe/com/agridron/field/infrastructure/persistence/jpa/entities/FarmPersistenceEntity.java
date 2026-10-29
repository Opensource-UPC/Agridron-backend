package pe.com.agridron.field.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * JPA representation of a farm and its assigned supervisors.
 */
@Entity
@Table(name = "farm")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FarmPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false, length = 100)
    private String department;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "farm_supervisor", joinColumns = @JoinColumn(name = "farm_id"))
    @Column(name = "supervisor_id", nullable = false)
    private Set<Long> supervisorIds = new LinkedHashSet<>();

    public FarmPersistenceEntity(Long ownerId, String name, String location, String department) {
        this.ownerId = ownerId;
        this.name = name;
        this.location = location;
        this.department = department;
    }

    public void updateDetails(String name, String location, String department) {
        this.name = name;
        this.location = location;
        this.department = department;
    }

    /**
     * Replaces the supervisor collection, keeping the JPA-managed collection instance.
     */
    public void replaceSupervisors(Collection<Long> supervisorIds) {
        this.supervisorIds.clear();
        if (supervisorIds != null) {
            this.supervisorIds.addAll(supervisorIds);
        }
    }

    /**
     * Returns an unmodifiable view of the assigned supervisors.
     */
    public Set<Long> getSupervisorIds() {
        return Collections.unmodifiableSet(supervisorIds);
    }
}
