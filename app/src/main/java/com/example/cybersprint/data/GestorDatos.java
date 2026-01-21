package com.example.cybersprint.data;

import android.content.Context;
import androidx.annotation.NonNull;

// Importaciones de Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GestorDatos {

    private static GestorDatos instancia;
    private DatabaseReference baseDatosRef;
    private String usuarioID;

    // Variables locales (Caché)
    private int recordLocal = 0;
    private int monedasLocal = 0;

    // Guardamos la referencia del listener para poder quitarlo si hace falta
    private ValueEventListener misDatosListener;

    private GestorDatos(Context context) {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            baseDatosRef = database.getReference("jugadores");

            // Obtenemos el ID ACTUAL en el momento de crear el gestor
            usuarioID = obtenerIDActual();

            // 3. Descargar datos de la nube
            misDatosListener = baseDatosRef.child(usuarioID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.hasChild("record")) {
                            String rec = snapshot.child("record").getValue().toString();
                            recordLocal = Integer.parseInt(rec);
                        }
                        if (snapshot.hasChild("monedas")) {
                            String mon = snapshot.child("monedas").getValue().toString();
                            monedasLocal = Integer.parseInt(mon);
                        }
                    } else {
                        // Si es usuario nuevo, reseteamos locales
                        recordLocal = 0;
                        monedasLocal = 0;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized GestorDatos getInstance(Context context) {
        if (instancia == null) {
            instancia = new GestorDatos(context);
        }
        return instancia;
    }

    // --- NUEVO MÉTODO CRÍTICO: RESETEAR AL CERRAR SESIÓN ---
    public static void reset() {
        if (instancia != null) {
            // Opcional: Quitar el listener para que deje de consumir recursos
            if (instancia.baseDatosRef != null && instancia.misDatosListener != null) {
                instancia.baseDatosRef.child(instancia.usuarioID).removeEventListener(instancia.misDatosListener);
            }
            instancia = null; // ¡Matamos el Singleton!
        }
    }

    // --- MÉTODOS PÚBLICOS ---

    public int obtenerRecord() { return recordLocal; }
    public int obtenerMonedas() { return monedasLocal; }

    public void guardarNuevoRecord(int puntos) {
        if (puntos > recordLocal) {
            recordLocal = puntos;

            // SEGURIDAD EXTRA: Volvemos a pedir el ID por si acaso
            String idActual = obtenerIDActual();

            if (baseDatosRef != null) {
                baseDatosRef.child(idActual).child("record").setValue(recordLocal);
            }
        }
    }

    public void sumarMonedas(int cantidad) {
        monedasLocal += cantidad;

        // SEGURIDAD EXTRA: Volvemos a pedir el ID
        String idActual = obtenerIDActual();

        if (baseDatosRef != null) {
            baseDatosRef.child(idActual).child("monedas").setValue(monedasLocal);
        }
    }

    // Método auxiliar seguro
    private String obtenerIDActual() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return "anonimo_error";
        }
    }
}