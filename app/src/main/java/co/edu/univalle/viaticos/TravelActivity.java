package co.edu.univalle.viaticos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.edu.univalle.viaticos.data.AppDatabase;
import co.edu.univalle.viaticos.data.dao.TravelDao;
import co.edu.univalle.viaticos.data.entity.Travel;

public class TravelActivity extends AppCompatActivity {
    private RecyclerView recyclerViajes;
    private TravelAdapter travelAdapter;
    private TravelDao travelDao;
    private int userId;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.travel_activity);

        // Configurar el sistema de insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            Log.e("TravelActivity", "Error: Usuario no identificado");
            finish();
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(this);
        travelDao = db.travelDao();

        recyclerViajes = findViewById(R.id.recyclerViajes);
        recyclerViajes.setLayoutManager(new LinearLayoutManager(this));
        travelAdapter = new TravelAdapter(new ArrayList<>(), travel -> {
            Intent intent = new Intent(TravelActivity.this, DetailsActivity.class);
            intent.putExtra("TRAVEL_ID", travel.getTravelId());
            startActivity(intent);
        });
        recyclerViajes.setAdapter(travelAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(TravelActivity.this, NewTravelActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        ImageButton exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> {
            Intent intent = new Intent(TravelActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        loadTravels();
    }

    private void loadTravels() {
        executorService.execute(() -> {
            List<Travel> travels = travelDao.getTravelsByEmployeeId(userId);
            Log.d("TravelActivity", "Total travels: " + travels.size());
            Log.d("TravelActivity", "User ID: " + userId);

            runOnUiThread(() -> {
                travelAdapter.updateTravels(travels);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTravels(); 
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}