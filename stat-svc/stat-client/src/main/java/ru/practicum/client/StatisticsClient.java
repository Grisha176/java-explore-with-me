package ru.practicum.client;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.client.base.BaseClient;
import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class StatisticsClient extends BaseClient {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /*@Autowired
    public StatisticsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {

        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    // Отправляем запрос на сохранение информации о хите
    public ResponseEntity<Object> create(HttpServletRequest request) {
        EndpointHitDto endpointHitCreateDto = EndpointHitDto.builder()
                .app(request.getHeader("app")) // Получаем app из заголовка запроса
                .uri(request.getRequestURI()) // Получаем URI из запроса
                .ip(request.getRemoteAddr()) // Получаем IP-адрес из запроса
                .timestamp(LocalDateTime.now().toString()) // Устанавливаем текущее время
                .build();
        return post("/hit", endpointHitCreateDto);
    }*/

    private String appName;

    @Autowired
    public StatisticsClient(@Value("${stats-server.url}") String serverUrl,
                            @Value("${app.name}") String appName,
                            RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build(),
                serverUrl
        );
        this.appName = appName;
    }

    public ResponseEntity<Object> create(HttpServletRequest request) {

        EndpointHitCreateDto endpointHitCreateDto = EndpointHitCreateDto.builder()
                .app(appName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .build();
        return post(endpointHitCreateDto);
    }

    // Получаем статистику
/*    public ResponseEntity<Object> getStats(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Получение статистики statc");
        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", String.join(",", uris),
                "unique", unique
        );
        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }*/

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("StatClient получение статистики");
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start.format(formatter))
                .queryParam("end", end.format(formatter))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris);
        }

        String url = builder.build().toUriString();

        return get(url);
    }

}
