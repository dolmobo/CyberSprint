package com.example.cybersprint.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cybersprint.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShopActivity extends AppCompatActivity {

    private long misMonedas = 0;
    private TextView txtMonedas;
    private DatabaseReference userRef;

    // Estados locales (0=No tiene, 1=Tiene, 2=Equipada)
    private boolean tieneSkin2 = false;
    private boolean tieneSkin3 = false;
    private int skinEquipada = 1; // 1=Default, 2=Azul, 3=Roja

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        txtMonedas = findViewById(R.id.txtMonedasTienda);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("jugadores").child(uid);

        // Configurar los 3 items visualmente
        setupItem(findViewById(R.id.itemSkin1), 1, "PROTOTIPO (Original)", 0, R.drawable.skin_default);
        setupItem(findViewById(R.id.itemSkin2), 2, "CIBER-AZUL", 200, R.drawable.skin_2);
        setupItem(findViewById(R.id.itemSkin3), 3, "NEÓN-ROJO", 500, R.drawable.skin_3);

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());

        // Escuchar cambios en Firebase
        cargarDatos();
    }

    private void cargarDatos() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                // 1. Cargar Monedas
                if (snapshot.child("monedas").exists()) {
                    misMonedas = Long.parseLong(snapshot.child("monedas").getValue().toString());
                    txtMonedas.setText("MONEDAS " + misMonedas);
                }

                // 2. Cargar Compras (Si existe el nodo es que la tiene)
                tieneSkin2 = snapshot.child("skins").child("skin_2").exists();
                tieneSkin3 = snapshot.child("skins").child("skin_3").exists();

                // 3. Cargar Equipada
                if (snapshot.child("skinEquipada").exists()) {
                    skinEquipada = Integer.parseInt(snapshot.child("skinEquipada").getValue().toString());
                } else {
                    skinEquipada = 1; // Por defecto
                }

                // Actualizar botones UI
                actualizarBotones();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void setupItem(View view, int idSkin, String nombre, int precio, int idImagen) {
        TextView txtNombre = view.findViewById(R.id.txtNombreSkin);
        TextView txtPrecio = view.findViewById(R.id.txtPrecioSkin);
        ImageView img = view.findViewById(R.id.imgSkin);
        Button btn = view.findViewById(R.id.btnAccionSkin);

        txtNombre.setText(nombre);
        img.setImageResource(idImagen);

        if (precio == 0) txtPrecio.setText("GRATIS");
        else txtPrecio.setText("PRECIO: " + precio);

        // Asignar el click listener una sola vez
        btn.setOnClickListener(v -> procesarClick(idSkin, precio));
    }

    private void actualizarBotones() {
        // Actualizamos el texto y color de cada botón según el estado
        updateButtonState(findViewById(R.id.itemSkin1), 1, true); // La 1 siempre se tiene
        updateButtonState(findViewById(R.id.itemSkin2), 2, tieneSkin2);
        updateButtonState(findViewById(R.id.itemSkin3), 3, tieneSkin3);
    }

    private void updateButtonState(View view, int idSkin, boolean tieneLaSkin) {
        Button btn = view.findViewById(R.id.btnAccionSkin);

        if (skinEquipada == idSkin) {
            btn.setText("EQUIPADO");
            btn.setBackgroundColor(0xFF00FF00); // Verde
            btn.setEnabled(false);
        } else if (tieneLaSkin) {
            btn.setText("SELECCIONAR");
            btn.setBackgroundColor(0xFF00E5FF); // Cian
            btn.setEnabled(true);
        } else {
            btn.setText("COMPRAR");
            btn.setBackgroundColor(0xFFFF0055); // Rojo/Rosa
            btn.setEnabled(true);
        }
    }

    private void procesarClick(int idSkin, int precio) {
        boolean tieneLaSkin = (idSkin == 1) || (idSkin == 2 && tieneSkin2) || (idSkin == 3 && tieneSkin3);

        if (tieneLaSkin) {
            // EQUIPAR
            userRef.child("skinEquipada").setValue(idSkin);
            Toast.makeText(this, "Skin Equipada", Toast.LENGTH_SHORT).show();
        } else {
            // INTENTAR COMPRAR
            if (misMonedas >= precio) {
                // Restar monedas
                userRef.child("monedas").setValue(misMonedas - precio);
                // Guardar que la tenemos
                userRef.child("skins").child("skin_" + idSkin).setValue(true);
                // Equipar automáticamente
                userRef.child("skinEquipada").setValue(idSkin);

                Toast.makeText(this, "¡Compra realizada!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No tienes suficientes créditos", Toast.LENGTH_SHORT).show();
            }
        }
    }
}