package co.edu.univalle.viaticos.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import co.edu.univalle.viaticos.data.entity.Category;
import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categories")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories")
    List<Category> getAllCategoriesSync();

    @Query("SELECT * FROM categories WHERE name = :name")
    Category getCategoryByName(String name);

    @Query("SELECT * FROM categories WHERE categoryId = :id")
    Category getCategoryById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(Category category);

    @Update
    void updateCategory(Category category);

    @Delete
    void deleteCategory(Category category);
} 