package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dao.CompilationRepository;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationRequestDto;
import ru.practicum.compilation.dto.UpdateCompilationRequestDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mappers.CompilationMapper;
import ru.practicum.mappers.EventMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Transactional
    @Override
    public CompilationDto create(NewCompilationRequestDto newCompilationRequestDto) {
        log.info("Попытка создания новой подпорки событий {}", newCompilationRequestDto);

        List<Event> events = eventRepository.findAllByIdIn(newCompilationRequestDto.getEvents());
        Compilation compilation = compilationMapper.mapToCompilation(newCompilationRequestDto);
        compilation.setEvents(events);
        compilation = compilationRepository.save(compilation);
        log.info("Успешное сохранение подборки {}",compilation);

        List<EventShortDto> eventShortDtos = events.stream().map(eventMapper::toEventShortDto).toList();

        return compilationMapper.mapToCompilationDto(compilation, eventShortDtos);
    }

    @Transactional
    @Override
    public void deleteComp(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка с id:"+compId+" не найдена"));
        compilationRepository.delete(compilation);
        log.info("Подборка с id: {} удалена",compilation);
    }

    @Override
    public CompilationDto updateComp(Long compId,UpdateCompilationRequestDto updateCompilationRequestDto) {
        log.info("Попытка обновления подборки с id:{},{}", compId, updateCompilationRequestDto);
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка с id:"+compId+" не найдена"));

        compilation = updateCompilationFields(compilation, updateCompilationRequestDto);
        compilation = compilationRepository.save(compilation);
        log.info("Обновление прошло успешно {}",compilation);

        List<EventShortDto> eventShortDtos = compilation.getEvents().stream().map(eventMapper::toEventShortDto).toList();


        return compilationMapper.mapToCompilationDto(compilation,eventShortDtos);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getAllCompilationPublic(Boolean pinned, int from, int size) {
        log.info("Получение всех подборок с from={}, size={}, pinned={}", from, size, pinned);
        Pageable pageable = PageRequest.of(from, size);
        return compilationRepository.findByPinned(pinned,pageable).stream().map(compilation -> {
            List<EventShortDto> events = compilation.getEvents().stream().map(eventMapper::toEventShortDto).toList();
            CompilationDto compilationDto = compilationMapper.mapToCompilationDto(compilation,events);
            return compilationDto;
        }).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getById(Long compId) {
        log.info("Получение подборки с id:{}",compId);
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка с id:"+compId+" не найдена"));
        log.info("Подборка найдена: {}", compilation);
        List<EventShortDto> events = compilation.getEvents().stream().map(eventMapper::toEventShortDto).toList();
        return compilationMapper.mapToCompilationDto(compilation,events);
    }

    private Compilation updateCompilationFields(Compilation compilation, UpdateCompilationRequestDto updateCompilationRequestDto) {

        if(updateCompilationRequestDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllByIdIn(updateCompilationRequestDto.getEvents()));
        }
        if(updateCompilationRequestDto.getPinned() != null) {
            compilation.setPinned(updateCompilationRequestDto.getPinned());
        }
        if(updateCompilationRequestDto.getTitle() != null) {
            compilation.setTitle(updateCompilationRequestDto.getTitle());
        }

        return compilation;
    }


}
