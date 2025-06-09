package co.edu.univalle.viaticos.data;

import android.content.Context;
import android.util.Log;
import androidx.room.Room;
import co.edu.univalle.viaticos.data.dao.*;
import co.edu.univalle.viaticos.data.entity.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";
    private final AppDatabase database;
    private final ExecutorService executorService;

    public DatabaseInitializer(Context context) {
        database = Room.databaseBuilder(
            context.getApplicationContext(),
            AppDatabase.class,
            "viaticos_database"
        ).build();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void initializeDatabase(InitializationCallback callback) {
        executorService.execute(() -> {
            try {
                // Verificar si ya existe un usuario
                UserDao userDao = database.userDao();
                User existingUser = userDao.getUserByEmail("admin@viaticos.com");
                
                if (existingUser == null) {
                    // Crear usuario administrador por defecto
                    User adminUser = new User(
                        "Administrador",
                        "123456789",
                        "admin@viaticos.com",
                        2000000.0,
                        "admin123"
                    );
                    long userId = userDao.insertUser(adminUser);
                    Log.d(TAG, "Usuario administrador creado con ID: " + userId);

                    // Crear categorías por defecto
                    CategoryDao categoryDao = database.categoryDao();
                    String[][] categorias = {
                        {"Transporte", "Gastos de transporte público y privado"},
                        {"Alimentación", "Gastos de comidas y bebidas"},
                        {"Hospedaje", "Gastos de hotel y alojamiento"},
                        {"Otros", "Otros gastos no categorizados"}
                    };

                    for (String[] categoria : categorias) {
                        Category cat = new Category(categoria[0], categoria[1]);
                        categoryDao.insertCategory(cat);
                    }
                    Log.d(TAG, "Categorías por defecto creadas");

                    callback.onSuccess();
                } else {
                    Log.d(TAG, "La base de datos ya está inicializada");
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al inicializar la base de datos: " + e.getMessage());
                callback.onError(e);
            }
        });
    }

    public interface InitializationCallback {
        void onSuccess();
        void onError(Exception e);
    }
} 