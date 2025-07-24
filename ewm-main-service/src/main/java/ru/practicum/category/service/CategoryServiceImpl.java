package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dao.CategoryRepository;
import org.springframework.data.domain.Pageable;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.DuplicatedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mappers.CategoryMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;


    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Попытка создать категорию с именем: {}", newCategoryDto.getName());
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new DuplicatedException("Название категории уже зарегистрировано: " + newCategoryDto.getName());
        }
        Category category = categoryMapper.mapToCategory(newCategoryDto);
        Category createdCategory = categoryRepository.save(category);
        log.info("Категория успешно создана с ID: {}", createdCategory.getId());
        return categoryMapper.mapToCategoryDto(createdCategory);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, UpdateCategoryDto updateCategoryDto) {
        log.info("Попытка обновить категорию с ID: {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID " + catId + " не найдена!"));

        String newName = updateCategoryDto.getName().trim();
        category.setName(newName);

        categoryRepository.save(category);
        log.info("Категория с ID {} успешно обновлена.", catId);
        return categoryMapper.mapToCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Category with id " + catId + " not found"));
        List<Event> events = eventRepository.findAllByCategoryId(category.getId());
        if (!events.isEmpty()) {
            throw new ConflictException("К категории привязаны события");
        }
        categoryRepository.deleteById(catId);
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getCategoryById(Long catId) {
        return categoryMapper.mapToCategoryDto(categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Category with id " + catId + " not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        return categoryRepository.findAll(pageable).stream().map(categoryMapper::mapToCategoryDto).toList();
    }


}
