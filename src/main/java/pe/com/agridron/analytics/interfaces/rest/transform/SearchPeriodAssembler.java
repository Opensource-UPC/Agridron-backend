package pe.com.agridron.analytics.interfaces.rest.transform;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Converts the inclusive date range received in the query string into the half-open
 * instant range used by the search criteria (UTC).
 */
public final class SearchPeriodAssembler {

    private SearchPeriodAssembler() {
    }

    public static Instant toFromInstant(LocalDate from) {
        return from == null ? null : from.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static Instant toToInstant(LocalDate to) {
        return to == null ? null : to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
