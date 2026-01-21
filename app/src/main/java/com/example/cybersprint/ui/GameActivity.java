package com.example.cybersprint.ui;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class GameActivity extends AppCompatActivity {

    // Declaramos la variable de nuestro motor de juego
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. IMPORTANTE: Creamos la vista del juego
        gameView = new GameView(this);

        // 2. IMPORTANTE: Ponemos 'gameView' en la pantalla, NO el XML
        // Si aquí pusieras R.layout.activity_game, saldría blanco.
        setContentView(gameView);

        // Configuración de pantalla completa
        EdgeToEdge.enable(this);
        ocultarBarrasSistema();
    }

    // 3. ¡VITAL! ESTO ES "GIRAR LA LLAVE"
    // Si no pones esto, el hilo del juego nunca empieza y la pantalla se queda quieta (blanca)
    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    // 4. Esto pausa el juego si sales de la app
    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    private void ocultarBarrasSistema() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }
}