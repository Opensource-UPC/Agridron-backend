package pe.com.agridron.inventory.application.internal.commandservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.domain.model.commands.CreateNozzleCommand;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleStock;
import pe.com.agridron.inventory.domain.repositories.NozzleRepository;
import pe.com.agridron.shared.application.result.ApplicationError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NozzleCommandServiceImplTest {

    @Mock
    private NozzleRepository nozzleRepository;

    @InjectMocks
    private NozzleCommandServiceImpl service;

    @Test
    void registersANozzleWithItsInitialStock() {
        when(nozzleRepository.save(any(Nozzle.class))).thenAnswer(invocation -> {
            var n = invocation.getArgument(0, Nozzle.class);
            return Nozzle.reconstitute(4L, n.getType(), n.getDiameterMm(), n.getStock());
        });

        var result = service.handle(new CreateNozzleCommand("flat-fan", 5.0, 6, 2, 1));

        assertThat(result.isSuccess()).isTrue();
        var nozzle = result.toOptional().orElseThrow();
        assertThat(nozzle.getId()).isEqualTo(4L);
        assertThat(nozzle.getType()).isEqualTo("flat-fan");
        assertThat(nozzle.getStock()).isEqualTo(new NozzleStock(6, 2, 1));
    }

    @Test
    void rejectsATypeLongerThanAllowed() {
        var result = service.handle(new CreateNozzleCommand("x".repeat(61), 5.0, 1, 0, 0));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.fold(n -> null, ApplicationError::code)).isEqualTo("VALIDATION_ERROR");
        verify(nozzleRepository, never()).save(any(Nozzle.class));
    }
}