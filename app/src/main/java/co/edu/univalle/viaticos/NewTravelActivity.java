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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewTravelActivity extends AppCompatActivity {

    private TextInputEditText fechaInicioInput;
    private TextInputEditText fechaFinInput;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_travel_activity);
        
        // Configurar el sistema de insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        fechaInicioInput = findViewById(R.id.fechaInicioInput);
        fechaFinInput = findViewById(R.id.fechaFinInput);
        ImageButton backButton = findViewById(R.id.backButton);
        calendar = Calendar.getInstance();

        // Configurar listeners
        fechaInicioInput.setOnClickListener(v -> showDatePicker(fechaInicioInput, true));
        fechaFinInput.setOnClickListener(v -> showDatePicker(fechaFinInput, false));
        backButton.setOnClickListener(v -> finish());
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

        // Si es la fecha de fin, establecer la fecha mínima como la fecha de inicio
        if (!isStartDate && fechaInicioInput.getText() != null && !fechaInicioInput.getText().toString().isEmpty()) {
            try {
                String[] fechaInicioParts = fechaInicioInput.getText().toString().split("\\.");
                if (fechaInicioParts.length == 3) {
                    Calendar minCalendar = Calendar.getInstance();
                    minCalendar.set(
                        Integer.parseInt(fechaInicioParts[2]), // año
                        Integer.parseInt(fechaInicioParts[1]) - 1, // mes (0-11)
                        Integer.parseInt(fechaInicioParts[0]) // día
                    );
                    datePickerDialog.getDatePicker().setMinDate(minCalendar.getTimeInMillis());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        datePickerDialog.show();
    }
}