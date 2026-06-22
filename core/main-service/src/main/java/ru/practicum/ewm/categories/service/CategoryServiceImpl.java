package ru.practicum.ewm.categories.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.categories.dto.CategoryMapper;
import ru.practicum.ewm.categories.dto.NewCategoryDto;
import ru.practicum.ewm.categories.model.Category;
import ru.practicum.ewm.categories.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.ewm.exceptions.exceptions.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto postCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toCategory(newCategoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.postCategory(category));
    }

    @Override
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с id = " + catId + " не найдена");
        }
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConditionsNotMetException("Есть события в этой категории, удалить нельзя");
        }
        categoryRepository.deleteCategory(catId);
    }

    @Override
    public CategoryDto patchCategory(Long catId, NewCategoryDto newCategoryDto) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с id = " + catId + " не найдена");
        }
        Category category = CategoryMapper.toCategory(newCategoryDto);
        Category updated = categoryRepository.patchCategory(catId, category);
        return CategoryMapper.toCategoryDto(updated);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        return categoryRepository.getCategories(from, size).stream().map(CategoryMapper::toCategoryDto).toList();
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с id = " + catId + " не найдена");
        }
        return CategoryMapper.toCategoryDto(categoryRepository.getCategory(catId));
    }


}
