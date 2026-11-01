package pe.com.agridron.field.application.internal.queryservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.model.queries.GetFarmByIdQuery;
import pe.com.agridron.field.domain.model.queries.GetFarmsByOwnerQuery;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.domain.model.queries.Pagination;
import pe.com.agridron.field.domain.model.queries.SortDirection;
import pe.com.agridron.field.domain.model.queries.SortOrder;
import pe.com.agridron.field.domain.repositories.FarmRepository;
import pe.com.agridron.shared.application.result.ApplicationError;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmQueryServiceImplTest {

    @Mock
    private FarmRepository farmRepository;

    @InjectMocks
    private FarmQueryServiceImpl service;

    @Test
    void returnsFarmById() {
        var farm = Farm.reconstitute(7L, 10L, "Finca El Sol", "Lima", "Lima", Set.of(20L));
        when(farmRepository.findById(7L)).thenReturn(Optional.of(farm));

        var result = service.handle(new GetFarmByIdQuery(7L));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getSupervisorIds()).containsExactly(20L);
    }

    @Test
    void returnsNotFoundWhenFarmDoesNotExist() {
        when(farmRepository.findById(7L)).thenReturn(Optional.empty());

        var result = service.handle(new GetFarmByIdQuery(7L));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.fold(farm -> null, ApplicationError::code)).isEqualTo("FARM_NOT_FOUND");
    }

    @Test
    void listsFarmsByOwner() {
        var farms = List.of(
                Farm.reconstitute(7L, 10L, "Alfa", "Lima", "Lima", Set.of()),
                Farm.reconstitute(8L, 10L, "Zeta", "Lima", "Lima", Set.of()));
        when(farmRepository.findAllByOwnerId(10L, Pagination.unpaged(), null))
                .thenReturn(PageResult.unpaged(farms));

        var result = service.handle(new GetFarmsByOwnerQuery(10L));

        assertThat(result.content()).extracting(Farm::getName).containsExactly("Alfa", "Zeta");
        verify(farmRepository).findAllByOwnerId(10L, Pagination.unpaged(), null);
    }

    @Test
    void delegatesPaginationAndOrdering() {
        var pagination = Pagination.of(1, 1);
        var sortOrder = new SortOrder("name", SortDirection.DESC);
        var page = new PageResult<>(
                List.of(Farm.reconstitute(8L, 10L, "Zeta", "Lima", "Lima", Set.of())),
                1,
                1,
                2);
        when(farmRepository.findAllByOwnerId(10L, pagination, sortOrder)).thenReturn(page);

        var result = service.handle(new GetFarmsByOwnerQuery(10L, pagination, sortOrder));

        assertThat(result.content()).extracting(Farm::getName).containsExactly("Zeta");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(2);
    }
}
