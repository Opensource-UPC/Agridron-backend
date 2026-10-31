package pe.com.agridron.analytics.application.internal.commandservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.commands.RegisterMissionHistoryCommand;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;
import pe.com.agridron.analytics.domain.repositories.MissionHistoryRepository;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionHistoryCommandServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-20T15:20:00Z");

    @Mock
    private MissionHistoryRepository repository;

    @InjectMocks
    private MissionHistoryCommandServiceImpl service;

    @Test
    void registersAFinishedMission() {
        var saved = MissionHistory.reconstitute(1L, 1L, 2L, MissionFinalStatus.COMPLETED, NOW);
        when(repository.existsByMissionId(1L)).thenReturn(false);
        when(repository.save(any(MissionHistory.class))).thenReturn(saved);

        var result = service.handle(new RegisterMissionHistoryCommand(1L, 2L, MissionFinalStatus.COMPLETED, NOW));

        assertThat(result.toOptional()).contains(saved);
    }

    @Test
    void rejectsAMissionThatIsAlreadyArchived() {
        when(repository.existsByMissionId(1L)).thenReturn(true);

        var result = service.handle(new RegisterMissionHistoryCommand(1L, 2L, MissionFinalStatus.COMPLETED, NOW));

        assertThat(result.<String>fold(h -> "OK", error -> error.code())).isEqualTo("HISTORY_CONFLICT");
        verify(repository, never()).save(any());
    }

    @Test
    void returnsValidationErrorForInvalidData() {
        var result = service.handle(new RegisterMissionHistoryCommand(1L, 2L, null, NOW));

        assertThat(result.<String>fold(h -> "OK", error -> error.code())).isEqualTo("VALIDATION_ERROR");
        verify(repository, never()).save(any());
    }
}
