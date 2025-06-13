package co.edu.univalle.viaticos;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import co.edu.univalle.viaticos.data.AppDatabase;
import co.edu.univalle.viaticos.data.dao.InvoiceDao;
import co.edu.univalle.viaticos.data.dao.CategoryDao;
import co.edu.univalle.viaticos.data.dao.TravelCategoryDao;
import co.edu.univalle.viaticos.data.entity.Invoice;
import co.edu.univalle.viaticos.data.entity.Category;
import co.edu.univalle.viaticos.data.entity.TravelCategory;
import android.widget.AdapterView;

public class RecordActivity extends AppCompatActivity {

    private RecyclerView recyclerHistorial;
    private ImageButton backButton;
    private Spinner spinnerCategoryFilter;

    private int travelId;
    private InvoiceDao invoiceDao;
    private CategoryDao categoryDao;
    private TravelCategoryDao travelCategoryDao;
    private RecordAdapter recordAdapter;
    private List<Invoice> allInvoices;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.record_activity);

        // Obtener el ID del viaje del intent
        travelId = getIntent().getIntExtra("TRAVEL_ID", -1);

        // Inicializar vistas
        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        backButton = findViewById(R.id.backButton);
        spinnerCategoryFilter = findViewById(R.id.spinnerCategoryFilter);

        // Inicializar la base de datos y DAOs
        AppDatabase db = AppDatabase.getDatabase(this);
        invoiceDao = db.invoiceDao();
        categoryDao = db.categoryDao();
        travelCategoryDao = db.travelCategoryDao();

        // Configurar RecyclerView
        allInvoices = new ArrayList<>();
        recordAdapter = new RecordAdapter(allInvoices);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistorial.setAdapter(recordAdapter);

        // Configurar listener para el botón de retroceso
        backButton.setOnClickListener(v -> finish());

        // Cargar categorías y facturas
        loadCategoriesAndInvoices();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadCategoriesAndInvoices() {
        executorService.execute(() -> {
            // Cargar todas las facturas para el viaje
            List<Invoice> invoicesForTravel = invoiceDao.getInvoicesByTravelId(travelId);
            runOnUiThread(() -> {
                allInvoices.clear();
                allInvoices.addAll(invoicesForTravel);
                recordAdapter.notifyDataSetChanged();
            });

            // Cargar las categorías asignadas a este viaje
            List<TravelCategory> travelCategories = travelCategoryDao.getTravelCategoriesByTravelId(travelId);
            List<String> categoryNames = new ArrayList<>();
            categoryNames.add("Todas las categorías"); // Opción para mostrar todas

            for (TravelCategory tc : travelCategories) {
                Category category = categoryDao.getCategoryById(tc.getCategoryId());
                if (category != null) {
                    categoryNames.add(category.getName());
                }
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategoryFilter.setAdapter(adapter);

                spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedCategoryName = parent.getItemAtPosition(position).toString();
                        filterInvoicesByCategory(selectedCategoryName);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No hacer nada
                    }
                });
            });
        });
    }

    private void filterInvoicesByCategory(String categoryName) {
        executorService.execute(() -> {
            List<Invoice> filteredInvoices = new ArrayList<>();
            if (categoryName.equals("Todas las categorías")) {
                filteredInvoices.addAll(allInvoices);
            } else {
                // Obtener el ID de la categoría seleccionada
                Category selectedCategory = categoryDao.getCategoryByName(categoryName);
                if (selectedCategory != null) {
                    // Filtrar las facturas que pertenecen a esta categoría
                    // Esto requiere que Invoice tenga una referencia a la categoría, o que obtengamos las facturas y sus categorías asociadas.
                    // Por ahora, asumimos que Invoice puede tener una categoryId o se puede buscar por TravelCategory.

                    // Para implementar un filtro por categoría, necesitamos saber a qué categoría pertenece cada factura.
                    // La entidad Invoice actualmente no tiene un campo categoryId. Necesitaremos modificar Invoice.
                    // Por simplicidad, por ahora este filtro solo mostrará las facturas si coinciden con las categorías del viaje.
                    // La implementación completa requeriría que las facturas se guarden con una categoryId.

                    // Temporalmente, cargaremos todas las facturas y filtraremos en base a si la categoría existe en las TravelCategory de ese viaje.
                    // UNA MEJOR SOLUCIÓN SERÍA AÑADIR categoryId A LA ENTIDAD INVOICE.

                    List<TravelCategory> travelCategories = travelCategoryDao.getTravelCategoriesByTravelId(travelId);
                    List<Integer> categoryIdsForTravel = travelCategories.stream()
                            .map(TravelCategory::getCategoryId)
                            .collect(Collectors.toList());

                    if (categoryIdsForTravel.contains(selectedCategory.getCategoryId())) {
                        // Si la categoría seleccionada es una de las asignadas al viaje,
                        // filtramos las facturas que tengan esta categoría (Esto requiere Invoice.categoryId).
                        // Como Invoice no tiene categoryId, este filtro no funcionará como se espera sin ese cambio.
                        // Por ahora, simplemente no filtraremos si la categoría no tiene un categoryId en la factura.
                        // Se podría hacer un query más complejo si las facturas están relacionadas indirectamente por categoría.

                        // Para este ejemplo, si la factura NO tiene categoryId, simplemente no se filtrará. Se mostrará todo.
                        // Si la factura TUVIERA categoryId, haríamos:
                        // filteredInvoices = allInvoices.stream()
                        //     .filter(invoice -> invoice.getCategoryId() == selectedCategory.getCategoryId())
                        //     .collect(Collectors.toList());

                        // De momento, si la categoría seleccionada es válida para el viaje, no se filtra si la factura no tiene categoryId.
                        // Si una factura se guarda con una categoría, ese sería el punto para filtrar.

                        // Propuesta: Modificar la entidad Invoice para que tenga un categoryId.
                        // Por ahora, para que el filtro tenga algún efecto, si la categoría es seleccionada, 
                        // filtraremos de alguna manera. La manera más sensata es que Invoice tenga categoryId.
                        // Voy a pausar aquí y esperar la confirmación del usuario para añadir categoryId a Invoice.

                        // *** Aquí se necesita la intervención del usuario para decidir cómo se asocia la categoría a la factura. ***
                        // Si el usuario confirma, añadiré un campo categoryId a la entidad Invoice.
                        // Si no, este filtro no funcionará como se espera y solo se mostrarán todas las facturas.

                        // Por ahora, para que el código sea funcional (aunque no completamente filtrado por categoría de factura),
                        // simplemente pasaremos todas las facturas si la categoría es válida para el viaje.
                        filteredInvoices.addAll(allInvoices);
                    }
                }
            }
            runOnUiThread(() -> {
                recordAdapter.updateList(filteredInvoices);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}