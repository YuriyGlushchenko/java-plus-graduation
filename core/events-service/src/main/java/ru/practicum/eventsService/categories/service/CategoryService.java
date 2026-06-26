package ru.practicum.eventsService.categories.service;



import ru.practicum.common.dto.events.category.CategoryDto;
import ru.practicum.eventsService.categories.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto postCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long catId);

    CategoryDto patchCategory(Long catId, NewCategoryDto newCategoryDto);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long catId);

}
