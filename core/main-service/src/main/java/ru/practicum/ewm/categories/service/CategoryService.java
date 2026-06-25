package ru.practicum.ewm.categories.service;

import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.categories.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto postCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long catId);

    CategoryDto patchCategory(Long catId, NewCategoryDto newCategoryDto);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long catId);

}
