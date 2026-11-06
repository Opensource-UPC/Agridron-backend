package pe.com.agridron.inventory.application.internal.queryservices;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.domain.model.queries.GetAllNozzlesQuery;
import pe.com.agridron.inventory.domain.model.queries.GetNozzleByIdQuery;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleStock;
import pe.com.agridron.inventory.domain.repositories.NozzleRepository;
import pe.com.agridron.shared.application.result.ApplicationError;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NozzleQueryServiceImplTest {

    @Mock
    private NozzleRepository nozzleRepository;

    @InjectMocks
    private NozzleQueryServiceImpl service;

    @Test
    void returnsTheNozzleWhenItExists() {
        var nozzle = Nozzle.reconstitute(1L, "flat-fan", 5.0, new NozzleStock(6, 0, 0));
        when(nozzleRepository.findById(1L)).thenReturn(Optional.of(nozzle));

        var result = service.handle(new GetNozzleByIdQuery(1L));

        assertThat(result.toOptional()).containsSame(nozzle);
    }

    @Test
    void returnsNotFoundWhenTheNozzleDoesNotExist() {
        when(nozzleRepository.findById(99L)).thenReturn(Optional.empty());

        var result = service.handle(new GetNozzleByIdQuery(99L));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.fold(found -> null, ApplicationError::code)).isEqualTo("NOZZLE_NOT_FOUND");
    }

    @Test
    void returnsAllTheRecords() {
        var nozzle = Nozzle.reconstitute(1L, "flat-fan", 5.0, new NozzleStock(6, 0, 0));
        when(nozzleRepository.findAll()).thenReturn(List.of(nozzle));

        assertThat(service.handle(new GetAllNozzlesQuery())).containsExactly(nozzle);
    }
}