package co.edu.univalle.viaticos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewInvoiceActivity extends AppCompatActivity {

    private TextInputEditText fechaInput;
    private TextInputLayout fechaLayout;
    private Calendar calendar;
    private AutoCompleteTextView tipoGastoInput;

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

        // Inicializar vistas y calendario
        initializeViews();
        setupDatePicker();
        setupBackButton();
        setupTipoGasto();
    }

    private void initializeViews() {
        fechaInput = findViewById(R.id.fechaInput);
        fechaLayout = findViewById(R.id.fechaLayout);
        calendar = Calendar.getInstance();
        tipoGastoInput = findViewById(R.id.tipoGastoInput);
    }

    private void setupTipoGasto() {
        // Obtener el array de tipos de gastos desde resources
        String[] tiposGastos = getResources().getStringArray(R.array.tipos_gastos);
        
        // Crear y configurar el adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            tiposGastos
        );
        
        tipoGastoInput.setAdapter(adapter);
        
        // Hacer que el campo no sea editable manualmente
        tipoGastoInput.setKeyListener(null);
    }

    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        };

        // Configurar el click en el campo de fecha
        fechaInput.setOnClickListener(v -> showDatePicker(dateSetListener));

        // Configurar el click en el ícono del calendario
        fechaLayout.setEndIconOnClickListener(v -> showDatePicker(dateSetListener));
    }

    private void showDatePicker(DatePickerDialog.OnDateSetListener dateSetListener) {
        new DatePickerDialog(
            NewInvoiceActivity.this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateLabel() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        fechaInput.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
    }
}