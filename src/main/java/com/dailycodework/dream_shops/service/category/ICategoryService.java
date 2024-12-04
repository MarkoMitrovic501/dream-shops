package com.dailycodework.dream_shops.service.category;

import com.dailycodework.dream_shops.model.Category;
import java.util.List;

public interface ICategoryService {

    Category getCategoryById(Long id);

    List<Category> getAllCategories();

    Category getCategoryByName(String name);

    Category addCategory(Category category);

    Category updateCategory(Category category, Long id);

    void deleteCategoryById(Long id);

}
