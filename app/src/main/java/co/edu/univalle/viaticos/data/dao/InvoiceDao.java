package co.edu.univalle.viaticos.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import co.edu.univalle.viaticos.data.entity.Invoice;
import java.util.List;

@Dao
public interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE travelId = :travelId")
    LiveData<List<Invoice>> getInvoicesByTravel(int travelId);

    @Query("SELECT * FROM invoices WHERE travelId = :travelId")
    List<Invoice> getInvoicesByTravelId(int travelId);

    @Query("SELECT * FROM invoices WHERE invoiceId = :id")
    Invoice getInvoiceById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertInvoice(Invoice invoice);

    @Update
    void updateInvoice(Invoice invoice);

    @Delete
    void deleteInvoice(Invoice invoice);

    @Query("DELETE FROM invoices WHERE travelId = :travelId")
    void deleteAllInvoicesForTravel(int travelId);
} 