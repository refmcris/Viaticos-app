package co.edu.univalle.viaticos;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Button;
import java.text.SimpleDateFormat;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import co.edu.univalle.viaticos.data.AppDatabase;
import co.edu.univalle.viaticos.data.dao.TravelDao;
import co.edu.univalle.viaticos.data.dao.UserDao;
import co.edu.univalle.viaticos.data.dao.InvoiceDao;
import co.edu.univalle.viaticos.data.entity.Travel;
import co.edu.univalle.viaticos.data.entity.User;
import co.edu.univalle.viaticos.data.entity.Invoice;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.widget.LinearLayout;
import co.edu.univalle.viaticos.data.dao.CategoryDao;
import co.edu.univalle.viaticos.data.entity.Category;
import co.edu.univalle.viaticos.data.dao.TravelCategoryDao;
import co.edu.univalle.viaticos.data.entity.TravelCategory;
import java.util.ArrayList;
import java.text.NumberFormat;

public class DetailsActivity extends AppCompatActivity {
    private TextView cityName;
    private TextView dateRange;
    private TextView limiteValue;
    private TextView gastosValue;
    private TextView totalValue;
    private TextView categoriesHeader;
    private LinearLayout categoriesContainer;
    private ImageButton backButton;
    private FloatingActionButton fab;
    private Button buttonHistorial;

    private TravelDao travelDao;
    private UserDao userDao;
    private InvoiceDao invoiceDao;
    private CategoryDao categoryDao;
    private TravelCategoryDao travelCategoryDao;
    private int travelId;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Travel travel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.details_activity);

        // Inicializar vistas
        cityName = findViewById(R.id.cityName);
        dateRange = findViewById(R.id.dateRange);
        limiteValue = findViewById(R.id.limiteValue);
        gastosValue = findViewById(R.id.gastosValue);
        totalValue = findViewById(R.id.totalValue);
        categoriesHeader = findViewById(R.id.categoriesHeader);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        backButton = findViewById(R.id.backButton);
        fab = findViewById(R.id.fab);
        buttonHistorial = findViewById(R.id.buttonHistorial);

        // Obtener el ID del viaje del intent
        travelId = getIntent().getIntExtra("TRAVEL_ID", -1);

        // Inicializar la base de datos y DAOs
        AppDatabase db = AppDatabase.getDatabase(this);
        travelDao = db.travelDao();
        userDao = db.userDao();
        invoiceDao = db.invoiceDao();
        categoryDao = db.categoryDao();
        travelCategoryDao = db.travelCategoryDao();

        // Cargar datos del viaje
        loadTravelDetails();

        // Configurar listener para el botón de retroceso
        backButton.setOnClickListener(v -> finish());

        // Configurar listener para el FloatingActionButton (agregar gasto)
        fab.setOnClickListener(v -> {
            if (travel != null && !travel.isCompleted()) {
                Intent intent = new Intent(DetailsActivity.this, NewInvoiceActivity.class);
                intent.putExtra("TRAVEL_ID", travelId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No se pueden agregar gastos a un viaje finalizado", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar listener para el botón "Ver historial de gastos"
        buttonHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, RecordActivity.class);
            intent.putExtra("TRAVEL_ID", travelId);
            startActivity(intent);
        });

        setupEditButton();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupButtons();
    }

    private void loadTravelDetails() {
        if (travelId == -1) {
            // Manejar error si no se recibe un TRAVEL_ID válido
            return;
        }

        executorService.execute(() -> {
            travel = travelDao.getTravelById(travelId);
            if (travel != null) {
                User user = userDao.getUserById(travel.getEmployeeId());
                List<Invoice> invoices = invoiceDao.getInvoicesByTravelId(travelId);

                double tempTotalExpenses = 0.0; // Variable temporal para el cálculo
                for (Invoice invoice : invoices) {
                    tempTotalExpenses += invoice.getAmount();
                }
                final double totalExpenses = tempTotalExpenses; // Variable final para usar en la lambda

                List<TravelCategory> travelCategories = travelCategoryDao.getTravelCategoriesByTravelId(travelId);
                List<String> categoryTexts = new ArrayList<>();
                
                if (travelCategories != null && !travelCategories.isEmpty()) {
                    for (TravelCategory tc : travelCategories) {
                        Category category = categoryDao.getCategoryById(tc.getCategoryId());
                        if (category != null) {
                            categoryTexts.add(String.format(Locale.getDefault(),
                                    "%s: %.1f%%", category.getName(), tc.getPercentage()));
                        }
                    }
                }

                runOnUiThread(() -> {
                    cityName.setText(travel.getDestinationCity());

                    SimpleDateFormat fullDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    String dateRangeText = String.format("%s - %s",
                            fullDateFormat.format(travel.getStartDate()),
                            fullDateFormat.format(travel.getEndDate()));
                    dateRange.setText(dateRangeText);

                    if (user != null) {
                        limiteValue.setText(String.format(Locale.getDefault(), "$%,.2f", travel.getTravelBudget()));
                    } else {
                        limiteValue.setText("$0.00");
                    }

                    gastosValue.setText(String.format(Locale.getDefault(), "$%,.2f", totalExpenses));
                    totalValue.setText(String.format(Locale.getDefault(), "$%,.2f", totalExpenses));

                    // Mostrar categorías y porcentajes
                    categoriesContainer.removeAllViews();
                    if (!categoryTexts.isEmpty()) {
                        categoriesHeader.setVisibility(View.VISIBLE);
                        for (String categoryText : categoryTexts) {
                            TextView categoryTextView = new TextView(DetailsActivity.this);
                            categoryTextView.setText(categoryText);
                            categoryTextView.setTextSize(14f);
                            categoriesContainer.addView(categoryTextView);
                        }
                    } else {
                        categoriesHeader.setVisibility(View.GONE);
                    }

                    // Actualizar visibilidad de botones según estado
                    Button buttonFinalizar = findViewById(R.id.buttonFinalizar);
                    if (buttonFinalizar != null) {
                        buttonFinalizar.setVisibility(travel.isCompleted() ? View.GONE : View.VISIBLE);
                    }

                    FloatingActionButton fab = findViewById(R.id.fab);
                    fab.setVisibility(travel.isCompleted() ? View.GONE : View.VISIBLE);

                    // Mostrar estado del viaje
                    TextView estadoText = findViewById(R.id.estadoText);
                    if (estadoText != null) {
                        estadoText.setText(travel.isCompleted() ? "Finalizado" : "En curso");
                        estadoText.setTextColor(getResources().getColor(
                            travel.isCompleted() ? android.R.color.holo_green_dark : android.R.color.holo_blue_dark
                        ));
                    }
                });
            }
        });
    }

    private void setupEditButton() {
        ImageButton editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> {
            if (travel != null && !travel.isCompleted()) {
                Intent intent = new Intent(DetailsActivity.this, EditTravelActivity.class);
                intent.putExtra("TRAVEL_ID", travelId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No se puede editar un viaje finalizado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons() {
        ImageButton editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> {
            if (travel != null && !travel.isCompleted()) {
                Intent intent = new Intent(DetailsActivity.this, EditTravelActivity.class);
                intent.putExtra("TRAVEL_ID", travelId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No se puede editar un viaje finalizado", Toast.LENGTH_SHORT).show();
            }
        });

        Button buttonHistorial = findViewById(R.id.buttonHistorial);
        buttonHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, RecordActivity.class);
            intent.putExtra("TRAVEL_ID", travelId);
            startActivity(intent);
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (travel != null && !travel.isCompleted()) {
                Intent intent = new Intent(DetailsActivity.this, NewInvoiceActivity.class);
                intent.putExtra("TRAVEL_ID", travelId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No se pueden agregar gastos a un viaje finalizado", Toast.LENGTH_SHORT).show();
            }
        });

        // Agregar botón para finalizar viaje
        Button buttonFinalizar = findViewById(R.id.buttonFinalizar);
        if (buttonFinalizar != null) {
            buttonFinalizar.setVisibility(travel != null && !travel.isCompleted() ? View.VISIBLE : View.GONE);
            buttonFinalizar.setOnClickListener(v -> {
                if (travel != null && !travel.isCompleted()) {
                    showFinalizarDialog();
                }
            });
        }
    }

    private void showFinalizarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Finalizar Viaje")
               .setMessage("¿Está seguro que desea finalizar este viaje? Esta acción no se puede deshacer.")
               .setPositiveButton("Sí", (dialog, which) -> finalizarViaje())
               .setNegativeButton("No", null)
               .show();
    }

    private void finalizarViaje() {
        executorService.execute(() -> {
            // Obtener el total gastado
            List<Invoice> invoices = invoiceDao.getInvoicesByTravelId(travelId);
            final double[] totalGastado = {0.0};
            for (Invoice invoice : invoices) {
                totalGastado[0] += invoice.getAmount();
            }

            // Actualizar el viaje
            travel.setCompleted(true);
            travelDao.updateTravel(travel);

            // Mostrar resumen
            runOnUiThread(() -> showResumenDialog(totalGastado[0]));
        });
    }


    private void showResumenDialog(double totalGastado) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_travel_summary, null);
        
        TextView limiteValue = view.findViewById(R.id.limiteValue);
        TextView gastosValue = view.findViewById(R.id.gastosValue);
        TextView diferenciaValue = view.findViewById(R.id.diferenciaValue);
        TextView estadoValue = view.findViewById(R.id.estadoValue);
        Button buttonAceptar = view.findViewById(R.id.buttonAceptar);

        // Formatear valores monetarios
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        String limiteStr = formatter.format(travel.getTravelBudget());
        String gastosStr = formatter.format(totalGastado);
        double diferencia = travel.getTravelBudget() - totalGastado;
        String diferenciaStr = formatter.format(diferencia);

        // Establecer valores
        limiteValue.setText(limiteStr);
        gastosValue.setText(gastosStr);
        diferenciaValue.setText(diferenciaStr);
        
        // Determinar estado
        if (diferencia >= 0) {
            estadoValue.setText("Dentro del presupuesto");
            estadoValue.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            estadoValue.setText("Excedió el presupuesto");
            estadoValue.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        AlertDialog dialog = builder.setView(view).create();
        buttonAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}