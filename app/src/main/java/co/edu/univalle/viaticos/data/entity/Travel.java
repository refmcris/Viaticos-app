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
    private double travelBudget;
    private boolean isCompleted;
    private Date completionDate;

    public Travel(int employeeId, String destinationCity, Date startDate, Date endDate, double totalSpent, boolean status, double travelBudget) {
        this.employeeId = employeeId;
        this.destinationCity = destinationCity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalSpent = totalSpent;
        this.status = status;
        this.travelBudget = travelBudget;
        this.isCompleted = false;
        this.completionDate = null;
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

    public double getTravelBudget() {
        return travelBudget;
    }

    public void setTravelBudget(double travelBudget) {
        this.travelBudget = travelBudget;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
        if (completed) {
            this.completionDate = new Date();
        }
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }
} 