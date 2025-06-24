package co.edu.univalle.viaticos;

import android.app.Application;
import co.edu.univalle.viaticos.data.AppDatabase;

public class ViaticosApplication extends Application {

    private static ViaticosApplication instance;
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Inicializar la base de datos
        database = AppDatabase.getDatabase(this);
    }

    public static ViaticosApplication getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }
}
