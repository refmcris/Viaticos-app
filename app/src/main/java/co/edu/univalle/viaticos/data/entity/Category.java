package co.edu.univalle.viaticos.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    private int categoryId;
    private String name;
    private String description;
    private double defaultPercentage;

    public Category(String name, String description, double defaultPercentage) {
        this.name = name;
        this.description = description;
        this.defaultPercentage = defaultPercentage;
    }

    // Getters and Setters
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDefaultPercentage() {
        return defaultPercentage;
    }

    public void setDefaultPercentage(double defaultPercentage) {
        this.defaultPercentage = defaultPercentage;
    }
} 