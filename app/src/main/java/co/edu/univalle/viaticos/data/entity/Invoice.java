package co.edu.univalle.viaticos.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(
    tableName = "invoices",
    foreignKeys = @ForeignKey(
        entity = Travel.class,
        parentColumns = "travelId",
        childColumns = "travelId",
        onDelete = ForeignKey.CASCADE
    )
)
public class Invoice {
    @PrimaryKey(autoGenerate = true)
    private int invoiceId;
    private int travelId;
    private Date date;
    private double amount;
    private String description;
    private int categoryId;

    public Invoice(int travelId, Date date, double amount, String description, int categoryId) {
        this.travelId = travelId;
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
    }

    // Getters and Setters
    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getTravelId() {
        return travelId;
    }

    public void setTravelId(int travelId) {
        this.travelId = travelId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
} 