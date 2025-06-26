package co.edu.univalle.viaticos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Intent;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import co.edu.univalle.viaticos.data.AppDatabase;
import co.edu.univalle.viaticos.data.dao.CategoryDao;
import co.edu.univalle.viaticos.data.dao.InvoiceDao;
import co.edu.univalle.viaticos.data.dao.TravelCategoryDao;
import co.edu.univalle.viaticos.data.dao.TravelDao;
import co.edu.univalle.viaticos.data.entity.Category;
import co.edu.univalle.viaticos.data.entity.Invoice;
import co.edu.univalle.viaticos.data.entity.TravelCategory;
import co.edu.univalle.viaticos.data.entity.Travel;

public class NewInvoiceActivity extends AppCompatActivity {

    private TextInputEditText nombreGastoInput;
    private TextInputEditText cantidadInput;
    private TextInputEditText fechaInput;
    private TextInputLayout fechaLayout;
    private AutoCompleteTextView tipoGastoInput;
    private Button buttonGuardar;
    private ImageButton backButton;

    private Calendar calendar;
    private int travelId;
    private InvoiceDao invoiceDao;
    private CategoryDao categoryDao;
    private TravelCategoryDao travelCategoryDao;
    private TravelDao travelDao;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private HashMap<String, Integer> categoryNameToIdMap = new HashMap<>();
    private Date travelStartDate;
    private Date travelEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_invoice_activity);
        
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

       
        initializeViews();
        setupDatePicker();
        setupBackButton();
        setupCategorySpinner();

       
        AppDatabase db = AppDatabase.getDatabase(this);
        invoiceDao = db.invoiceDao();
        categoryDao = db.categoryDao();
        travelCategoryDao = db.travelCategoryDao();
        travelDao = db.travelDao();

        loadTravelDates();

        buttonGuardar.setOnClickListener(v -> saveInvoice());
    }

    private void initializeViews() {
        nombreGastoInput = findViewById(R.id.nombreGastoInput);
        cantidadInput = findViewById(R.id.cantidadInput);
        fechaInput = findViewById(R.id.fechaInput);
        fechaLayout = findViewById(R.id.fechaLayout);
        tipoGastoInput = findViewById(R.id.tipoGastoInput);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        backButton = findViewById(R.id.backButton);
        calendar = Calendar.getInstance();
    }

    private void setupCategorySpinner() {
        executorService.execute(() -> {
            List<TravelCategory> travelCategories = travelCategoryDao.getTravelCategoriesByTravelId(travelId);
            List<String> categoryNames = new ArrayList<>();

            for (TravelCategory tc : travelCategories) {
                Category category = categoryDao.getCategoryById(tc.getCategoryId());
                if (category != null) {
                    categoryNames.add(category.getName());
                    categoryNameToIdMap.put(category.getName(), category.getCategoryId());
                }
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        categoryNames
                );
                tipoGastoInput.setAdapter(adapter);

                
                tipoGastoInput.setKeyListener(null);
            });
        });
    }

    private void loadTravelDates() {
        executorService.execute(() -> {
            Travel travel = travelDao.getTravelById(travelId);
            if (travel != null) {
                travelStartDate = travel.getStartDate();
                travelEndDate = travel.getEndDate();
            }
        });
    }

    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        };

        fechaInput.setOnClickListener(v -> {
            if (travelStartDate != null && travelEndDate != null) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                    NewInvoiceActivity.this,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                );

                
                datePickerDialog.getDatePicker().setMinDate(travelStartDate.getTime());
                datePickerDialog.getDatePicker().setMaxDate(travelEndDate.getTime());

                datePickerDialog.show();
            } else {
                Toast.makeText(this, "Error al cargar las fechas del viaje", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDateLabel() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        fechaInput.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void saveInvoice() {
        String description = nombreGastoInput.getText().toString().trim();
        String amountStr = cantidadInput.getText().toString().trim();
        String dateStr = fechaInput.getText().toString().trim();
        String categoryName = tipoGastoInput.getText().toString().trim();

        if (description.isEmpty() || amountStr.isEmpty() || dateStr.isEmpty() || categoryName.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = 0.0;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato de cantidad inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Date date = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        
        if (date.before(travelStartDate) || date.after(travelEndDate)) {
            Toast.makeText(this, "La fecha debe estar dentro del rango del viaje", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer categoryId = categoryNameToIdMap.get(categoryName);
        if (categoryId == null) {
            Toast.makeText(this, "Categoría seleccionada inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        final Date finalDate = date;
        final double finalAmount = amount;
        final int finalCategoryId = categoryId;

        executorService.execute(() -> {
            Invoice invoice = new Invoice(travelId, finalDate, finalAmount, description, finalCategoryId);
            invoiceDao.insertInvoice(invoice);

            runOnUiThread(() -> {
                Toast.makeText(NewInvoiceActivity.this, "Gasto guardado exitosamente", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("INVOICE_ADDED", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}