package co.edu.univalle.viaticos.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import co.edu.univalle.viaticos.data.entity.Travel;
import java.util.List;

@Dao
public interface TravelDao {
    @Query("SELECT * FROM travels")
    LiveData<List<Travel>> getAllTravels();

    @Query("SELECT * FROM travels WHERE employeeId = :employeeId")
    LiveData<List<Travel>> getTravelsByEmployee(int employeeId);

    @Query("SELECT * FROM travels WHERE travelId = :id")
    Travel getTravelById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTravel(Travel travel);

    @Update
    void updateTravel(Travel travel);

    @Delete
    void deleteTravel(Travel travel);
} 