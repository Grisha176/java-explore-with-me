package ru.practicum.category.controller;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.UpdateCategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Запрос на добавление новой категории {}", newCategoryDto);
        return categoryService.createCategory(newCategoryDto);
    }

    @PatchMapping("/admin/categories/{catId}")
    public CategoryDto updateCategory(@PathVariable Integer catId, @RequestBody @Valid UpdateCategoryDto updateCategoryDto) {
        log.info("Запрос на обновление категории с id {}", catId);
        return categoryService.updateCategory(catId,updateCategoryDto);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Integer catId) {
        log.info("Запрос на удаление категории с id {}", catId);
        categoryService.deleteCategory(catId);
    }

    @GetMapping("/categories")
    public List<CategoryDto> getAllCategories(@RequestHeader(value = "from",defaultValue = "0") int from,@RequestHeader(value = "size",defaultValue = "10") int size) {
        return categoryService.getAllCategories(from,size);

    }
    @GetMapping("/categories/{catId}")
    public CategoryDto getCategoryById(@PathVariable Integer catId) {
        return categoryService.getCategory(catId);

    }
}
