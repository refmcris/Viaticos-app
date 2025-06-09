package co.edu.univalle.viaticos.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import co.edu.univalle.viaticos.data.dao.*;
import co.edu.univalle.viaticos.data.entity.*;
import co.edu.univalle.viaticos.util.DateConverter;

@Database(
    entities = {
        User.class,
        Travel.class,
        Category.class,
        TravelCategory.class,
        Invoice.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract TravelDao travelDao();
    public abstract CategoryDao categoryDao();
    public abstract TravelCategoryDao travelCategoryDao();
    public abstract InvoiceDao invoiceDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "viaticos_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
} 