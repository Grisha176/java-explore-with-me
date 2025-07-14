package ru.practicum.category.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.dao.CategoryRepository;
import org.springframework.data.domain.Pageable;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;
import ru.practicum.category.model.Category;
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


    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Создание новой категории {}", newCategoryDto);
        Category category = categoryMapper.mapToCategory(newCategoryDto);
        if(categoryRepository.existsByName(category.getName())) {
            throw new DuplicatedException("Category with name " + category.getName() + " already exists");
        }
        category = categoryRepository.save(category);
        log.info("Категория успешно добавлене {} ",category);
        return categoryMapper.mapToCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Integer catId,UpdateCategoryDto updateCategoryDto) {
        log.info("Обновление категории с id: {}", catId);
        Category category = categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Category with id "+catId+" not found"));
        String newName = updateCategoryDto.getName().toLowerCase().trim();
        String oldName = category.getName().toLowerCase().trim();
        if(!oldName.equals(newName)) {
            log.info("Имя категории с id: {}, изменено с {},на {}", catId, oldName, newName);
            category.setName(newName);
        }
        category = categoryRepository.save(category);
        log.info("Категория c id {} успешно обновлена", catId);
        return categoryMapper.mapToCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Category with id "+catId+" not found"));
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto getCategory(Integer catId) {
        return categoryMapper.mapToCategoryDto(categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Category with id "+catId+" not found")));
    }

    @Override
    public List<CategoryDto> getAllCategories(int from,int size) {
        Pageable pageable = PageRequest.of(from,size, Sort.by("name").descending());
        return categoryRepository.findAll(pageable).stream().map(categoryMapper::mapToCategoryDto).toList();
    }


}
