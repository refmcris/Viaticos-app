package co.edu.univalle.viaticos.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import co.edu.univalle.viaticos.data.entity.TravelCategory;
import java.util.List;

@Dao
public interface TravelCategoryDao {
    @Query("SELECT * FROM travel_categories WHERE travelId = :travelId")
    LiveData<List<TravelCategory>> getCategoriesByTravel(int travelId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTravelCategory(TravelCategory travelCategory);

    @Update
    void updateTravelCategory(TravelCategory travelCategory);

    @Delete
    void deleteTravelCategory(TravelCategory travelCategory);

    @Query("DELETE FROM travel_categories WHERE travelId = :travelId")
    void deleteAllCategoriesForTravel(int travelId);
} 