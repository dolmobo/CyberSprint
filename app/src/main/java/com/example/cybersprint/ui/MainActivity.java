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

        // 1. CARGAMOS EL DISEÑO XML QUE ACABAMOS DE HACER
        setContentView(R.layout.activity_main);

        // 2. Buscamos los elementos
        Button btnJugar = findViewById(R.id.btnJugar);
        Button btnSalir = findViewById(R.id.btnSalir);
        txtStats = findViewById(R.id.txtStats);

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

        // 4. Cargamos los datos
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