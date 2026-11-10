package pe.com.agridron.analytics.domain.model.queries;

import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Optional filters for searching mission history. Absent (or empty) collections do not filter.
 * The {@code from} bound is inclusive and the {@code to} bound is exclusive.
 */
public record MissionHistorySearchCriteria(
        Collection<Long> missionIds,
        Collection<Long> parcelIds,
        MissionFinalStatus status,
        Instant from,
        Instant to) {

    public MissionHistorySearchCriteria {
        missionIds = normalize(missionIds, "missionIds");
        parcelIds = normalize(parcelIds, "parcelIds");
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
    }

    public static MissionHistorySearchCriteria none() {
        return new MissionHistorySearchCriteria(null, null, null, null, null);
    }

    private static Collection<Long> normalize(Collection<Long> ids, String field) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("%s must contain only positive ids".formatted(field));
        }
        return List.copyOf(ids);
    }
}
