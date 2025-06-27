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

        travelId = getIntent().getIntExtra("TRAVEL_ID", -1);

        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        backButton = findViewById(R.id.backButton);
        spinnerCategoryFilter = findViewById(R.id.spinnerCategoryFilter);

        AppDatabase db = AppDatabase.getDatabase(this);
        invoiceDao = db.invoiceDao();
        categoryDao = db.categoryDao();
        travelCategoryDao = db.travelCategoryDao();

        allInvoices = new ArrayList<>();
        recordAdapter = new RecordAdapter(allInvoices);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistorial.setAdapter(recordAdapter);

        backButton.setOnClickListener(v -> finish());

        loadCategoriesAndInvoices();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadCategoriesAndInvoices() {
        executorService.execute(() -> {
            List<Invoice> invoicesForTravel = invoiceDao.getInvoicesByTravelId(travelId);
            runOnUiThread(() -> {
                allInvoices.clear();
                allInvoices.addAll(invoicesForTravel);
                recordAdapter.notifyDataSetChanged();
            });

            List<TravelCategory> travelCategories = travelCategoryDao.getTravelCategoriesByTravelId(travelId);
            List<String> categoryNames = new ArrayList<>();
            categoryNames.add("Todas las categorías"); 

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
                
                Category selectedCategory = categoryDao.getCategoryByName(categoryName);
                if (selectedCategory != null) {
                    

                    List<TravelCategory> travelCategories = travelCategoryDao.getTravelCategoriesByTravelId(travelId);
                    List<Integer> categoryIdsForTravel = travelCategories.stream()
                            .map(TravelCategory::getCategoryId)
                            .collect(Collectors.toList());

                    if (categoryIdsForTravel.contains(selectedCategory.getCategoryId())) {
                       
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