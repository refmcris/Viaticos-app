package co.edu.univalle.viaticos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.text.InputType;
import android.view.LayoutInflater;

import java.text.ParseException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;

import co.edu.univalle.viaticos.data.AppDatabase;
import co.edu.univalle.viaticos.data.dao.TravelDao;
import co.edu.univalle.viaticos.data.dao.CategoryDao;
import co.edu.univalle.viaticos.data.dao.TravelCategoryDao;
import co.edu.univalle.viaticos.data.entity.Travel;
import co.edu.univalle.viaticos.data.entity.Category;
import co.edu.univalle.viaticos.data.entity.TravelCategory;
import co.edu.univalle.viaticos.data.dao.UserDao;
import co.edu.univalle.viaticos.data.entity.User;

public class NewTravelActivity extends AppCompatActivity {

    private TextInputEditText destinoInput;
    private TextInputEditText fechaInicioInput;
    private TextInputEditText fechaFinInput;
    private ChipGroup chipGroupCategories;
    private Button buttonCrear;
    private ImageButton backButton;

    private Calendar calendar;
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
        setContentView(R.layout.new_travel_activity);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        destinoInput = findViewById(R.id.destinoInput);
        fechaInicioInput = findViewById(R.id.fechaInicioInput);
        fechaFinInput = findViewById(R.id.fechaFinInput);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        buttonCrear = findViewById(R.id.buttonCrear);
        backButton = findViewById(R.id.backButton);
        calendar = Calendar.getInstance();

        AppDatabase db = AppDatabase.getDatabase(this);
        travelDao = db.travelDao();
        userDao = db.userDao();
        categoryDao = db.categoryDao();
        travelCategoryDao = db.travelCategoryDao();

        // Configurar listeners
        fechaInicioInput.setOnClickListener(v -> showDatePicker(fechaInicioInput, true));
        fechaFinInput.setOnClickListener(v -> showDatePicker(fechaFinInput, false));
        backButton.setOnClickListener(v -> finish());

        buttonCrear.setOnClickListener(v -> createNewTravel());

        loadCategoriesIntoChips();
    }

    private void loadCategoriesIntoChips() {
        executorService.execute(() -> {
            List<Category> categories = categoryDao.getAllCategoriesSync();
            User user = userDao.getUserById(userId);
            runOnUiThread(() -> {
                chipGroupCategories.removeAllViews();
                
                for (Category category : categories) {
                    Chip chip = new Chip(NewTravelActivity.this);
                    chip.setText(category.getName());
                    chip.setCheckable(true);
                    chip.setTag(category.getCategoryId());
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

    private void showDatePicker(TextInputEditText editText, boolean isStartDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String formattedDate = dateFormat.format(selectedCalendar.getTime());
                editText.setText(formattedDate);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        if (!isStartDate && fechaInicioInput.getText() != null && !fechaInicioInput.getText().toString().isEmpty()) {
            try {
                String[] fechaInicioParts = fechaInicioInput.getText().toString().split("\\.");
                if (fechaInicioParts.length == 3) {
                    Calendar minCalendar = Calendar.getInstance();
                    minCalendar.set(
                        Integer.parseInt(fechaInicioParts[2]), 
                        Integer.parseInt(fechaInicioParts[1]) - 1, 
                        Integer.parseInt(fechaInicioParts[0]) 
                    );
                    datePickerDialog.getDatePicker().setMinDate(minCalendar.getTimeInMillis());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        datePickerDialog.show();
    }

    private void createNewTravel() {
        String destino = destinoInput.getText().toString().trim();
        String fechaInicio = fechaInicioInput.getText().toString().trim();
        String fechaFin = fechaFinInput.getText().toString().trim();

        if (destino.isEmpty() || fechaInicio.isEmpty() || fechaFin.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategoryPercentages.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione al menos una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                User currentUser = userDao.getUserById(userId);
                if (currentUser == null) {
                    runOnUiThread(() -> Toast.makeText(NewTravelActivity.this, 
                        "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show());
                    return;
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                Date startDate = dateFormat.parse(fechaInicio);
                Date endDate = dateFormat.parse(fechaFin);

                if (startDate == null || endDate == null) {
                    runOnUiThread(() -> Toast.makeText(NewTravelActivity.this, 
                        "Error en el formato de fechas", Toast.LENGTH_SHORT).show());
                    return;
                }

                List<Travel> overlappingTravels = travelDao.getOverlappingTravels(
                    currentUser.getEmployeeId(), startDate, endDate);
                
                if (!overlappingTravels.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(NewTravelActivity.this, 
                        "Ya existe un viaje programado para estas fechas", Toast.LENGTH_SHORT).show());
                    return;
                }

                double totalBudget = 0;
                for (Double percentage : selectedCategoryPercentages.values()) {
                    totalBudget += currentUser.getSalary() * (percentage / 100.0);
                }

                Travel travel = new Travel(
                    currentUser.getEmployeeId(),
                    destino,
                    startDate,
                    endDate,
                    0.0,         // totalSpent (lo gastado al crear el viaje es 0)
                    false,       // status
                    totalBudget  // travelBudget (el límite calculado)
                );

                long travelId = travelDao.insertTravel(travel);

                for (HashMap.Entry<Integer, Double> entry : selectedCategoryPercentages.entrySet()) {
                    TravelCategory travelCategory = new TravelCategory(
                        (int) travelId, 
                        entry.getKey(), 
                        entry.getValue()
                    );
                    travelCategoryDao.insertTravelCategory(travelCategory);
                }

                runOnUiThread(() -> {
                    Toast.makeText(NewTravelActivity.this, 
                        "Viaje creado exitosamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(NewTravelActivity.this, TravelActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(NewTravelActivity.this, 
                    "Error al crear el viaje: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}