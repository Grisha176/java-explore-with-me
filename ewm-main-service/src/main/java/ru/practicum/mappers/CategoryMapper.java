package ru.practicum.mappers;

import org.mapstruct.Mapper;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;
import ru.practicum.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category mapToCategory(NewCategoryDto newCategoryDto);

    CategoryDto mapToCategoryDto(Category category);

    Category mapToCategory(UpdateCategoryDto updateCategoryDto);

}
