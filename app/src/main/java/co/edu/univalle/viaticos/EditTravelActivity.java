package co.edu.univalle.viaticos;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;
import java.text.SimpleDateFormat;

import co.edu.univalle.viaticos.data.AppDatabase;
import co.edu.univalle.viaticos.data.dao.TravelDao;
import co.edu.univalle.viaticos.data.dao.CategoryDao;
import co.edu.univalle.viaticos.data.dao.TravelCategoryDao;
import co.edu.univalle.viaticos.data.dao.UserDao;
import co.edu.univalle.viaticos.data.entity.Travel;
import co.edu.univalle.viaticos.data.entity.Category;
import co.edu.univalle.viaticos.data.entity.TravelCategory;
import co.edu.univalle.viaticos.data.entity.User;

public class EditTravelActivity extends AppCompatActivity {

    private ChipGroup chipGroupCategories;
    private Button buttonGuardar;
    private ImageButton backButton;

    private int travelId;
    private int userId;
    private UserDao userDao;
    private TravelDao travelDao;
    private CategoryDao categoryDao;
    private TravelCategoryDao travelCategoryDao;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private HashMap<Integer, Double> selectedCategoryPercentages = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.edit_travel_activity);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        travelId = getIntent().getIntExtra("TRAVEL_ID", -1);
        if (travelId == -1) {
            Toast.makeText(this, "Error: ID de viaje no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        backButton = findViewById(R.id.backButton);

        AppDatabase db = AppDatabase.getDatabase(this);
        travelDao = db.travelDao();
        userDao = db.userDao();
        categoryDao = db.categoryDao();
        travelCategoryDao = db.travelCategoryDao();

        backButton.setOnClickListener(v -> finish());
        buttonGuardar.setOnClickListener(v -> updateTravel());


        loadTravelData();
    }

    private void loadTravelData() {
        executorService.execute(() -> {
            Travel travel = travelDao.getTravelById(travelId);
            if (travel != null) {
                userId = travel.getEmployeeId();
                User user = userDao.getUserById(userId);
                List<TravelCategory> travelCategories = travelCategoryDao.getTravelCategoriesByTravelId(travelId);
                

                for (TravelCategory tc : travelCategories) {
                    selectedCategoryPercentages.put(tc.getCategoryId(), tc.getPercentage());
                }

                runOnUiThread(() -> {
                    loadCategoriesIntoChips();
                });
            }
        });
    }

    private void loadCategoriesIntoChips() {
        executorService.execute(() -> {
            List<Category> categories = categoryDao.getAllCategoriesSync();
            User user = userDao.getUserById(userId);
            runOnUiThread(() -> {
                
                chipGroupCategories.removeAllViews();
                
                for (Category category : categories) {
                    Chip chip = new Chip(EditTravelActivity.this);
                    chip.setText(category.getName());
                    chip.setCheckable(true);
                    chip.setTag(category.getCategoryId());
                    
                    
                    if (selectedCategoryPercentages.containsKey(category.getCategoryId())) {
                        chip.setChecked(true);
                    }
                    
                    chipGroupCategories.addView(chip);

                    
                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        int categoryId = (int) buttonView.getTag();
                        
                        if (isChecked) {
                            selectedCategoryPercentages.put(categoryId, category.getDefaultPercentage());
                        } else {
                            selectedCategoryPercentages.remove(categoryId);
                        }
                    });
                }
            });
        });
    }

    private void updateTravel() {
        if (selectedCategoryPercentages.isEmpty()) {
            Toast.makeText(this, "Por favor, seleccione al menos una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            User user = userDao.getUserById(userId);
            if (user == null) {
                runOnUiThread(() -> {
                    Toast.makeText(EditTravelActivity.this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
                });
                return;
            }

           
            double totalPercentage = 0.0;
            for (Double percentage : selectedCategoryPercentages.values()) {
                totalPercentage += percentage;
            }
            double travelBudget = user.getSalary() * (totalPercentage / 100.0);

           
            Travel travel = travelDao.getTravelById(travelId);
            if (travel != null) {
                travel.setTravelBudget(travelBudget);
                travelDao.updateTravel(travel);

                
                travelCategoryDao.deleteAllCategoriesForTravel(travelId);

                
                for (HashMap.Entry<Integer, Double> entry : selectedCategoryPercentages.entrySet()) {
                    TravelCategory travelCategory = new TravelCategory(
                        travelId, 
                        entry.getKey(), 
                        entry.getValue()
                    );
                    travelCategoryDao.insertTravelCategory(travelCategory);
                }

                runOnUiThread(() -> {
                    Toast.makeText(EditTravelActivity.this, "Categorías actualizadas con éxito", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditTravelActivity.this, DetailsActivity.class);
                    intent.putExtra("TRAVEL_ID", travelId);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
} 