package co.edu.univalle.viaticos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import co.edu.univalle.viaticos.data.AppDatabase;
import co.edu.univalle.viaticos.data.dao.UserDao;
import co.edu.univalle.viaticos.data.entity.User;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_main);

        AppDatabase db = AppDatabase.getDatabase(this);
        userDao = db.userDao();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.buttonSignIn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<User> allUsers = userDao.getAllUsersSync();
                        for (User u : allUsers) {
                            Log.d("LoginDebug", "Usuario en BD: " + u.getEmail() + " / " + u.getPassword());
                        }
                        Log.d("Login", "Email ingresado: " + email);
                        User user = userDao.getUserByEmail(email);
                        if (user != null) {
                            Log.d("Login", "Usuario encontrado: " + user.getEmail() + ", password: " + user.getPassword());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (user != null && user.getPassword().equals(password)) {
                                    Log.d(TAG, "Login exitoso para usuario: " + user.getEmployeeId());
                                    try {
                                        Intent intent = new Intent(LoginActivity.this, TravelActivity.class);
                                        intent.putExtra("USER_ID", user.getEmployeeId());
                                        Log.d(TAG, "Iniciando TravelActivity con USER_ID: " + user.getEmployeeId());
                                        startActivity(intent);
                                        finish();
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error al iniciar TravelActivity: " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.d(TAG, "Login fallido - Usuario no encontrado o contraseña incorrecta");
                                    Toast.makeText(LoginActivity.this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
