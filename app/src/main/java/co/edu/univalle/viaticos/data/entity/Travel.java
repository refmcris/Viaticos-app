package co.edu.univalle.viaticos.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(
    tableName = "travels",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "employeeId",
        childColumns = "employeeId",
        onDelete = ForeignKey.CASCADE
    )
)
public class Travel {
    @PrimaryKey(autoGenerate = true)
    private int travelId;
    private int employeeId;
    private String destinationCity;
    private Date startDate;
    private Date endDate;
    private double totalSpent;
    private boolean status;

    public Travel(int employeeId, String destinationCity, Date startDate, Date endDate, double totalSpent, boolean status) {
        this.employeeId = employeeId;
        this.destinationCity = destinationCity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalSpent = totalSpent;
        this.status = status;
    }

    // Getters and Setters
    public int getTravelId() {
        return travelId;
    }

    public void setTravelId(int travelId) {
        this.travelId = travelId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
} 