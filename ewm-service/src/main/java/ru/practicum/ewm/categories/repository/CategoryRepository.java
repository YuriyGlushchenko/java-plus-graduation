package ru.practicum.ewm.categories.repository;

import ru.practicum.ewm.categories.model.Category;

import java.util.List;

public interface CategoryRepository {

    Category postCategory(Category category);

    void deleteCategory(Long catId);

    Category patchCategory(Long catId, Category category);

    List<Category> getCategories(int from, int size);

    Category getCategory(Long catId);

    boolean existsById(Long catId);
}
