package com.example.cybersprint.ui;

import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.cybersprint.R;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        FrameLayout container = findViewById(R.id.gameContainer);

        // 1. CREAR EL JUEGO INMEDIATAMENTE (Skin por defecto)
        // Esto evita el NullPointerException porque gameView ya existe
        gameView = new GameView(this, 1);
        container.addView(gameView);

        // 2. BUSCAR LA SKIN REAL EN SEGUNDO PLANO
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("jugadores").child(uid).child("skinEquipada");

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {
                            int skinId = Integer.parseInt(snapshot.getValue().toString());
                            // ACTUALIZAR LA SKIN EN CALIENTE
                            if (gameView != null) {
                                gameView.actualizarSkinJugador(skinId);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // No hacemos nada, se queda con la default
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
    }
}