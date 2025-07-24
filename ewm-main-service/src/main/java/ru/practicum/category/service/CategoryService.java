package ru.practicum.category.service;


import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long catId, UpdateCategoryDto updateCategoryDto);

    void deleteCategory(Long catId);

    CategoryDto getCategoryById(Long catId);

    List<CategoryDto> getAllCategories(int from, int size);
}
