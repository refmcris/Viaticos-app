package co.edu.univalle.viaticos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.edu.univalle.viaticos.data.AppDatabase;
import co.edu.univalle.viaticos.data.dao.TravelDao;
import co.edu.univalle.viaticos.data.entity.Travel;

public class TravelActivity extends AppCompatActivity {
    private RecyclerView travelRecyclerView;
    private Button newTravelButton;
    private TravelDao travelDao;
    private int userId;
    private List<Travel> travelList;
    private TravelAdapter travelAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.travel_activity);

        // Obtener el ID del usuario del intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar la base de datos
        AppDatabase db = AppDatabase.getDatabase(this);
        travelDao = db.travelDao();

        // Inicializar vistas
        travelRecyclerView = findViewById(R.id.travelRecyclerView);
        newTravelButton = findViewById(R.id.newTravelButton);

        // Configurar RecyclerView
        travelList = new ArrayList<>();
        travelAdapter = new TravelAdapter(travelList, new TravelAdapter.OnTravelClickListener() {
            @Override
            public void onTravelClick(Travel travel) {
                // Abrir la actividad de detalles del viaje
                Intent intent = new Intent(TravelActivity.this, DetailsActivity.class);
                intent.putExtra("TRAVEL_ID", travel.getTravelId());
                startActivity(intent);
            }
        });

        travelRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        travelRecyclerView.setAdapter(travelAdapter);

        // Configurar el botón de nuevo viaje
        newTravelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TravelActivity.this, NewTravelActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        // Cargar los viajes del usuario
        loadUserTravels();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserTravels();
    }

    private void loadUserTravels() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Travel> travels = travelDao.getTravelsByEmployee(userId).getValue();
                if (travels != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            travelList.clear();
                            travelList.addAll(travels);
                            travelAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }
}