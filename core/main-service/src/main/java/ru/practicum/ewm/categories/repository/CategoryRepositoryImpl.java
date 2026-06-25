package ru.practicum.ewm.categories.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.categories.model.Category;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Category postCategory(Category category) {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});  // заменил Statement.RETURN_GENERATED_KEYS, в postgres не работает иначе
            ps.setString(1, category.getName());
            return ps;
        }, keyHolder);

        category.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return category;
    }

    @Override
    public void deleteCategory(Long catId) {
        String sql = "DELETE FROM categories WHERE id = ?";
        jdbcTemplate.update(sql, catId);
    }

    @Override
    public Category patchCategory(Long catId, Category category) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, category.getName(), catId);
        return new Category(catId, category.getName());
    }

    @Override
    public List<Category> getCategories(int from, int size) {
        String sql = "SELECT * FROM categories ORDER BY id LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Category(
                rs.getLong("id"),
                rs.getString("name")
        ), size, from);
    }

    @Override
    public Category getCategory(Long catId) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Category(
                rs.getLong("id"),
                rs.getString("name")
        ), catId);
    }

    @Override
    public boolean existsById(Long catId) {
        String sql = "SELECT COUNT(*) FROM categories WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, catId);
        return count > 0;
    }
}
