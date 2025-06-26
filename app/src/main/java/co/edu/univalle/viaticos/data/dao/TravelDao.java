package co.edu.univalle.viaticos.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import co.edu.univalle.viaticos.data.entity.Travel;
import java.util.Date;
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

    @Query("SELECT * FROM travels WHERE employeeId = :employeeId")
    List<Travel> getTravelsByEmployeeId(int employeeId);

    @Query("SELECT * FROM travels WHERE employeeId = :employeeId AND isCompleted = :isCompleted")
    List<Travel> getTravelsByEmployeeIdAndStatus(int employeeId, boolean isCompleted);

    @Query("SELECT * FROM travels WHERE employeeId = :employeeId AND " +
           "((startDate <= :endDate AND endDate >= :startDate) OR " +
           "(startDate >= :startDate AND startDate <= :endDate) OR " +
           "(endDate >= :startDate AND endDate <= :endDate))")
    List<Travel> getOverlappingTravels(int employeeId, Date startDate, Date endDate);

    @Query("SELECT * FROM travels WHERE employeeId = :employeeId AND " +
           "((startDate <= :endDate AND endDate >= :startDate) OR " +
           "(startDate >= :startDate AND startDate <= :endDate) OR " +
           "(endDate >= :startDate AND endDate <= :endDate)) AND " +
           "travelId != :currentTravelId")
    List<Travel> getOverlappingTravelsExcludingCurrent(int employeeId, Date startDate, Date endDate, int currentTravelId);
} 