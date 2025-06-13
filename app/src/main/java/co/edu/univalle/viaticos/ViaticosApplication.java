package co.edu.univalle.viaticos;

import android.app.Application;
import co.edu.univalle.viaticos.data.DatabaseInitializer;

public class ViaticosApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Inicializar la base de datos
        DatabaseInitializer initializer = new DatabaseInitializer(this);
        initializer.initializeDatabase(new DatabaseInitializer.InitializationCallback() {
            @Override
            public void onSuccess() {
                // Base de datos inicializada correctamente
            }

            @Override
            public void onError(Exception e) {
                // Manejar el error de inicialización
                e.printStackTrace();
            }
        });
    }
} 