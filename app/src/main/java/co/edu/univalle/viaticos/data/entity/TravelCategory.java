package co.edu.univalle.viaticos.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "travel_categories",
    primaryKeys = {"travelId", "categoryId"},
    foreignKeys = {
        @ForeignKey(entity = Travel.class,
            parentColumns = "travelId",
            childColumns = "travelId",
            onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Category.class,
            parentColumns = "categoryId",
            childColumns = "categoryId",
            onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index(value = {"travelId", "categoryId"}, unique = true)}
)
public class TravelCategory {
    private int travelId;
    private int categoryId;
    private double percentage;

    public TravelCategory(int travelId, int categoryId, double percentage) {
        this.travelId = travelId;
        this.categoryId = categoryId;
        this.percentage = percentage;
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

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
} 