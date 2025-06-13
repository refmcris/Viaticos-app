package co.edu.univalle.viaticos.data;

import android.content.Context;
import android.util.Log;
import androidx.room.Room;
import co.edu.univalle.viaticos.data.dao.*;
import co.edu.univalle.viaticos.data.entity.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
                User existingUser = userDao.getUserByEmail("a");
                
                if (existingUser == null) {
                    User adminUser = new User(
                        "Administrador",
                        "123456789",
                        "a",
                        5000000.0,
                        "a"
                    );
                    long userId = userDao.insertUser(adminUser);
                    Log.d(TAG, "Usuario administrador creado con ID: " + userId);

                    // Crear categorías por defecto
                    CategoryDao categoryDao = database.categoryDao();
                    Object[][] defaultCategories = {
                        {"Transporte", "Gastos de transporte público y privado", 30.0},
                        {"Alimentación", "Gastos de comidas y bebidas", 40.0},
                        {"Hospedaje", "Gastos de hotel y alojamiento", 25.0},
                        {"Otros", "Otros gastos no categorizados", 5.0}
                    };

                    for (Object[] categoria : defaultCategories) {
                        if (categoryDao.getCategoryByName((String)categoria[0]) == null) {
                            Category cat = new Category(
                                (String)categoria[0], 
                                (String)categoria[1],
                                (Double)categoria[2]
                            );
                            categoryDao.insertCategory(cat);
                        }
                    }
                    Log.d(TAG, "Categorías por defecto creadas o verificadas");

                    // Obtener IDs de las categorías para asignarlas a los viajes de ejemplo
                    int transporteId = categoryDao.getCategoryByName("Transporte").getCategoryId();
                    int alimentacionId = categoryDao.getCategoryByName("Alimentación").getCategoryId();
                    int hospedajeId = categoryDao.getCategoryByName("Hospedaje").getCategoryId();
                    int otrosId = categoryDao.getCategoryByName("Otros").getCategoryId();

                    // Crear viajes de ejemplo para el usuario administrador
                    TravelDao travelDao = database.travelDao();
                    TravelCategoryDao travelCategoryDao = database.travelCategoryDao();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    try {
                        Travel travel1 = new Travel(
                                (int) userId,
                                "Medellin",
                                sdf.parse("2025-03-20"),
                                sdf.parse("2025-04-10"),
                                500000.00,
                                true,
                                1000000.00
                        );
                        long travel1Id = travelDao.insertTravel(travel1);
                        travelCategoryDao.insertTravelCategory(new TravelCategory((int) travel1Id, transporteId, 50.0));
                        travelCategoryDao.insertTravelCategory(new TravelCategory((int) travel1Id, alimentacionId, 50.0));

                        // Facturas de ejemplo para el Viaje 1 (Medellín)
                        InvoiceDao invoiceDao = database.invoiceDao();
                        Invoice invoice1_1 = new Invoice((int) travel1Id, sdf.parse("2025-03-25"), 150000.00, "Pasaje de bus Medellín", transporteId);
                        invoiceDao.insertInvoice(invoice1_1);
                        Invoice invoice1_2 = new Invoice((int) travel1Id, sdf.parse("2025-03-26"), 50000.00, "Almuerzo restaurante", alimentacionId);
                        invoiceDao.insertInvoice(invoice1_2);
                        Invoice invoice1_3 = new Invoice((int) travel1Id, sdf.parse("2025-03-27"), 80000.00, "Hotel primera noche", hospedajeId);
                        invoiceDao.insertInvoice(invoice1_3);

                        Travel travel2 = new Travel(
                                (int) userId,
                                "Bogota",
                                sdf.parse("2025-05-01"),
                                sdf.parse("2025-05-15"),
                                750000.50,
                                false,
                                1500000.00
                        );
                        long travel2Id = travelDao.insertTravel(travel2);
                        travelCategoryDao.insertTravelCategory(new TravelCategory((int) travel2Id, hospedajeId, 70.0));
                        travelCategoryDao.insertTravelCategory(new TravelCategory((int) travel2Id, otrosId, 30.0));

                        // Facturas de ejemplo para el Viaje 2 (Bogotá)
                        Invoice invoice2_1 = new Invoice((int) travel2Id, sdf.parse("2025-05-05"), 200000.00, "Hotel en el centro", hospedajeId);
                        invoiceDao.insertInvoice(invoice2_1);
                        Invoice invoice2_2 = new Invoice((int) travel2Id, sdf.parse("2025-05-06"), 75000.00, "Entrada a museo", otrosId);
                        invoiceDao.insertInvoice(invoice2_2);

                        Travel travel3 = new Travel(
                                (int) userId,
                                "Cali",
                                sdf.parse("2025-06-10"),
                                sdf.parse("2025-06-20"),
                                300000.25,
                                true,
                                600000.00
                        );
                        long travel3Id = travelDao.insertTravel(travel3);
                        travelCategoryDao.insertTravelCategory(new TravelCategory((int) travel3Id, transporteId, 40.0));
                        travelCategoryDao.insertTravelCategory(new TravelCategory((int) travel3Id, alimentacionId, 60.0));

                        // Facturas de ejemplo para el Viaje 3 (Cali)
                        Invoice invoice3_1 = new Invoice((int) travel3Id, sdf.parse("2025-06-12"), 40000.00, "Cena típica", alimentacionId);
                        invoiceDao.insertInvoice(invoice3_1);
                        Invoice invoice3_2 = new Invoice((int) travel3Id, sdf.parse("2025-06-15"), 20000.00, "Taxi al aeropuerto", transporteId);
                        invoiceDao.insertInvoice(invoice3_2);

                        Log.d(TAG, "Viajes de ejemplo y categorías asignadas creados para el usuario administrador");
                        Log.d(TAG, "Facturas de ejemplo creadas para los viajes");
                    } catch (ParseException e) {
                        Log.e(TAG, "Error al parsear fechas de viaje o asignar categorías: " + e.getMessage());
                    }

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