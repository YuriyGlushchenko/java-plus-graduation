package ru.practicum.eventsService.categories.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.aop.annotation.Loggable;
import ru.practicum.common.dto.events.category.CategoryDto;
import ru.practicum.eventsService.categories.dto.NewCategoryDto;
import ru.practicum.eventsService.categories.service.CategoryService;


@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Loggable
    public CategoryDto postCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return categoryService.postCategory(newCategoryDto);
    }

    @DeleteMapping(path = "/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Loggable
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }

    @PatchMapping(path = "/{catId}")
    @Loggable
    public CategoryDto patchCategory(@PathVariable Long catId,
                                     @RequestBody @Valid NewCategoryDto newCategoryDto) {
        return categoryService.patchCategory(catId, newCategoryDto);
    }

}
