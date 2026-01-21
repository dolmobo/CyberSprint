package com.example.cybersprint.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.cybersprint.data.GestorDatos;
import com.google.firebase.auth.FirebaseAuth; // <--- 1. Importante: Importar Firebase

import com.example.cybersprint.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ponemos el modo pantalla completa (juego)
        ocultarBarrasSistema();

        // 1. Buscamos los botones por su ID
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnStore = findViewById(R.id.btnStore);
        Button btnSupport = findViewById(R.id.btnSupport);
        Button btnLogout = findViewById(R.id.btnLogout); // <--- Asegúrate de que este ID existe en tu XML

        // 2. Acción JUGAR: Abre la pantalla del juego
        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        // 3. Acción TIENDA: Mensaje temporal
        btnStore.setOnClickListener(v ->
                Toast.makeText(this, "Tienda: Próximamente", Toast.LENGTH_SHORT).show()
        );

        // 4. Acción APOYAR: Mensaje temporal
        btnSupport.setOnClickListener(v ->
                Toast.makeText(this, "¡Gracias por apoyar!", Toast.LENGTH_SHORT).show()
        );

        // 5. NUEVO: Acción SALIR (Firebase)
        btnLogout.setOnClickListener(v -> {
            // A. Le decimos a Firebase que cierre la sesión
            FirebaseAuth.getInstance().signOut();

            GestorDatos.reset();

            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

            // B. Volvemos al Login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

            // C. Cerramos esta pantalla
            finish();
        });
    }

    // Método para ocultar la barra de notificaciones y botones virtuales
    private void ocultarBarrasSistema() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }
}