import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.EndpointHitDto;
import ru.practicum.dao.StatsRepository;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;
import ru.practicum.service.StatsServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class StatisticServiceTest {

    @InjectMocks
    private StatsServiceImpl statsService;

    @Mock
    private StatsRepository statsRepository;

    @Mock
    private StatsMapper statsMapper;

    private static final LocalDateTime NOW = LocalDateTime.now();
    private EndpointHit endpointHit;
    private EndpointHitDto endpointHitDto;

    @BeforeEach
    void setUp() {

        endpointHitDto = EndpointHitDto.builder()
                .app("test-app")
                .uri("/test-uri")
                .ip("192.168.0.1")
                .timestamp(NOW.toString())
                .build();

        endpointHit = EndpointHit.builder()
                .id(1L)
                .app("test-app")
                .uri("/test-uri")
                .ip("192.168.0.1")
                .timestamp(NOW)
                .build();

    }

    @Test
    void addRequest_shouldMapAndSaveEndpointHit() {
        // Given
        when(statsMapper.mapToEndpointHit(endpointHitDto)).thenReturn(endpointHit);
        when(statsRepository.save(endpointHit)).thenReturn(endpointHit);
        when(statsMapper.mapToEndpointHitDto(endpointHit)).thenReturn(endpointHitDto);

        // When
        EndpointHitDto result = statsService.addRequest(endpointHitDto);

        // Then
        assertThat(result).isEqualTo(endpointHitDto);
        verify(statsMapper, times(1)).mapToEndpointHit(endpointHitDto);
        verify(statsRepository, times(1)).save(endpointHit);
        verify(statsMapper, times(1)).mapToEndpointHitDto(endpointHit);
    }

    @Test
    void getStats_withUrisAndUniqueIp_shouldCallFindStatsUniqueIp() {
        // Given
        List<String> uris = List.of("/uri1", "/uri2");

        List<ViewStats> expectedStats = List.of(
                new ViewStats("app1", "/uri1", 5L),
                new ViewStats("app2", "/uri2", 3L)
        );

        when(statsRepository.findStatsUniqueIp(NOW, NOW.plusHours(1), uris))
                .thenReturn(expectedStats);

        // When
        List<ViewStats> result = statsService.getStats(NOW, NOW.plusHours(1), uris, true);

        // Then
        assertEquals(2,result.size());
        assertThat(result).containsExactlyElementsOf(expectedStats);
        verify(statsRepository, times(1)).findStatsUniqueIp(NOW, NOW.plusHours(1), uris);
        verify(statsRepository, never()).findStatsUniqueIpAllUris(any(), any());
    }

    @Test
    void getStats_withoutUrisAndUniqueIp_shouldCallFindStatsUniqueIpAllUris() {
        // Given
        List<ViewStats> expectedStats = List.of(new ViewStats("app1", "/all", 10L));

        when(statsRepository.findStatsUniqueIpAllUris(NOW, NOW.plusDays(1)))
                .thenReturn(expectedStats);

        // When
        List<ViewStats> result = statsService.getStats(NOW, NOW.plusDays(1), null, true);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactlyElementsOf(expectedStats);
        verify(statsRepository, times(1)).findStatsUniqueIpAllUris(NOW, NOW.plusDays(1));
        verify(statsRepository, never()).findStatsUniqueIp(any(), any(), any());
    }

    @Test
    void getStats_withUrisAndNotUnique_shouldCallFindStats() {
        // Given
        List<String> uris = List.of("/uri1");
        List<ViewStats> expectedStats = List.of(new ViewStats("app1", "/uri1", 7L));

        when(statsRepository.findStats(NOW, NOW.plusMinutes(30), uris))
                .thenReturn(expectedStats);

        // When
        List<ViewStats> result = statsService.getStats(NOW, NOW.plusMinutes(30), uris, false);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactlyElementsOf(expectedStats);
        verify(statsRepository, times(1)).findStats(NOW, NOW.plusMinutes(30), uris);
    }

    @Test
    void getStats_withoutUrisAndNotUnique_shouldCallFindStatsAllUris() {
        // Given
        List<ViewStats> expectedStats = List.of(new ViewStats("app1", "/all", 15L));

        when(statsRepository.findStatsAllUris(NOW, NOW.plusHours(2)))
                .thenReturn(expectedStats);

        // When
        List<ViewStats> result = statsService.getStats(NOW, NOW.plusHours(2), null, false);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactlyElementsOf(expectedStats);
        verify(statsRepository, times(1)).findStatsAllUris(NOW, NOW.plusHours(2));
    }

    @Test
    void testAddRequest() {
        // Given
        EndpointHitDto dto = new EndpointHitDto(1L, "app", "/uri", "ip", LocalDateTime.now().toString());
        EndpointHit endpointHit = new EndpointHit(1L, "app", "/uri", "ip", LocalDateTime.now());

        when(statsMapper.mapToEndpointHit(dto)).thenReturn(endpointHit);
        when(statsRepository.save(endpointHit)).thenReturn(endpointHit);
        when(statsMapper.mapToEndpointHitDto(endpointHit)).thenReturn(dto);

        // When
        EndpointHitDto result = statsService.addRequest(dto);

        // Then
        assertNotNull(result);
        assertEquals(dto, result);
        verify(statsMapper, times(1)).mapToEndpointHit(dto);
        verify(statsRepository, times(1)).save(endpointHit);
        verify(statsMapper, times(1)).mapToEndpointHitDto(endpointHit);
    }

    @Test
    void testGetStats_UniqueTrueWithUris() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = Arrays.asList("/uri1", "/uri2");
        Boolean unique = true;

        List<ViewStats> expectedStats = Arrays.asList(
                new ViewStats("app", "/uri1", 10L),
                new ViewStats("app", "/uri2", 5L)
        );

        when(statsRepository.findStatsUniqueIp(start, end, uris)).thenReturn(expectedStats);

        // When
        List<ViewStats> result = statsService.getStats(start, end, uris, unique);

        // Then
        assertNotNull(result);
        assertEquals(expectedStats.size(), result.size());
        assertEquals(expectedStats, result);
        verify(statsRepository, times(1)).findStatsUniqueIp(start, end, uris);
    }

    @Test
    void testGetStats_UniqueTrueNoUris() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = null;
        Boolean unique = true;

        List<ViewStats> expectedStats = Arrays.asList(
                new ViewStats("app", "/uri1", 10L)
        );

        when(statsRepository.findStatsUniqueIpAllUris(start, end)).thenReturn(expectedStats);

        // When
        List<ViewStats> result = statsService.getStats(start, end, uris, unique);

        // Then
        assertNotNull(result);
        assertEquals(expectedStats.size(), result.size());
        assertEquals(expectedStats, result);
        verify(statsRepository, times(1)).findStatsUniqueIpAllUris(start, end);
    }

    @Test
    void testGetStats_UniqueFalseWithUris() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = Arrays.asList("/uri1", "/uri2");
        Boolean unique = false;

        List<ViewStats> expectedStats = Arrays.asList(
                new ViewStats("app", "/uri1", 20L)
        );

        when(statsRepository.findStats(start, end, uris)).thenReturn(expectedStats);

        // When
        List<ViewStats> result = statsService.getStats(start, end, uris, unique);

        // Then
        assertNotNull(result);
        assertEquals(expectedStats.size(), result.size());
        assertEquals(expectedStats, result);
        verify(statsRepository, times(1)).findStats(start, end, uris);
    }

    @Test
    void testGetStats_UniqueFalseNoUris() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = null;
        Boolean unique = false;

        List<ViewStats> expectedStats = Arrays.asList(
                new ViewStats("app", "/uri1", 30L)
        );

        when(statsRepository.findStatsAllUris(start, end)).thenReturn(expectedStats);

        // When
        List<ViewStats> result = statsService.getStats(start, end, uris, unique);

        // Then
        assertNotNull(result);
        assertEquals(expectedStats.size(), result.size());
        assertEquals(expectedStats, result);
        verify(statsRepository, times(1)).findStatsAllUris(start, end);
    }
}

