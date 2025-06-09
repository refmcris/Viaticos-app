package co.edu.univalle.viaticos.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "travel_categories",
    foreignKeys = {
        @ForeignKey(
            entity = Travel.class,
            parentColumns = "travelId",
            childColumns = "travelId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Category.class,
            parentColumns = "categoryId",
            childColumns = "categoryId",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class TravelCategory {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int travelId;
    private int categoryId;
    private double amount;

    public TravelCategory(int travelId, int categoryId, double amount) {
        this.travelId = travelId;
        this.categoryId = categoryId;
        this.amount = amount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTravelId() {
        return travelId;
    }

    public void setTravelId(int travelId) {
        this.travelId = travelId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
} 