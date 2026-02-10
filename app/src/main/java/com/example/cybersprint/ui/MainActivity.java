package com.example.cybersprint.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cybersprint.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView txtStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- BLOQUE DE SEGURIDAD (EL PORTERO) ---
        // Comprobamos si hay un usuario logueado. Si es null, lo echamos al Login.
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Importante: Cierra esta actividad para que no se quede detrás
            return;   // Importante: Detiene la ejecución para que no cargue el resto del código
        }
        // ----------------------------------------

        // 1. CARGAMOS EL DISEÑO XML
        setContentView(R.layout.activity_main);

        // 2. Buscamos los elementos
        Button btnJugar = findViewById(R.id.btnJugar);
        Button btnSalir = findViewById(R.id.btnSalir);
        // Asegúrate de inicializar txtStats aquí para que no de error luego
        txtStats = findViewById(R.id.txtStats);
        Button btnTienda = findViewById(R.id.btnTienda);

        // 3. Programamos los botones
        btnJugar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        btnSalir.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnTienda.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShopActivity.class);
            startActivity(intent);
        });

        // 4. Cargamos los datos (Ahora seguro que funcionará porque ya pasamos el filtro)
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("jugadores").child(uid);

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    long record = 0;
                    long monedas = 0;

                    if (snapshot.exists()) {
                        try {
                            if (snapshot.child("record").exists())
                                record = Long.parseLong(snapshot.child("record").getValue().toString());
                            if (snapshot.child("monedas").exists())
                                monedas = Long.parseLong(snapshot.child("monedas").getValue().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Actualizamos el texto del pie de página
                    txtStats.setText("RÉCORD: " + record + "   |   MONEDAS: " + monedas);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    txtStats.setText("Error al cargar datos");
                }
            });
        }
    }

}