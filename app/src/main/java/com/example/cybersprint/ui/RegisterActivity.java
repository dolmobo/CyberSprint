package com.example.cybersprint.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cybersprint.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 2. Vincular vistas
        etEmail = findViewById(R.id.etEmailReg);
        etPassword = findViewById(R.id.etPasswordReg);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // 3. Botón de Registro
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }

                crearUsuario(email, password);
            }
        });

        // 4. Botón para volver al Login
        tvLogin.setOnClickListener(v -> finish());
    }

    private void crearUsuario(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 1. Obtener el usuario recién creado
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            // 2. PARSEAR EL EMAIL para obtener el nombre de usuario
                            // Ejemplo: david@gmail.com -> David
                            String username = email.split("@")[0];
                            if (username.length() > 0) {
                                username = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
                            }

                            // 3. Preparar los datos iniciales para el Realtime Database
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("jugadores").child(uid);

                            Map<String, Object> datosIniciales = new HashMap<>();
                            datosIniciales.put("username", username);
                            datosIniciales.put("email", email);
                            datosIniciales.put("record", 0);
                            datosIniciales.put("monedas", 0);
                            datosIniciales.put("partidas_totales", 0);
                            datosIniciales.put("saltos_totales", 0);
                            datosIniciales.put("puntuacion_total", 0);

                            // 4. Guardar en la nube
                            mDatabase.setValue(datosIniciales).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Cuenta y perfil creados", Toast.LENGTH_SHORT).show();
                                }

                                // Continuar al Menú Principal independientemente de si el registro de datos fue instantáneo
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}