package ru.practicum.ewm.categories.dto;

import ru.practicum.ewm.categories.model.Category;

public class CategoryMapper {
    public static Category toCategory(CategoryDto categoryDto) {
        return Category.builder()
                .id(categoryDto.getId())
                .name(categoryDto.getName())
                .build();
    }

    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static NewCategoryDto toNewCategoryDto(Category category) {
        return NewCategoryDto.builder()
                .name(category.getName())
                .build();
    }
}
