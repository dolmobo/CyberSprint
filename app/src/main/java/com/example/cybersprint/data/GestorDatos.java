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

    public void sumarPartidaJugada() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Usamos baseDatosRef (que apunta a "jugadores") y el UID del usuario
        DatabaseReference partidasRef = baseDatosRef.child(user.getUid()).child("partidas_totales");

        partidasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long partidas = 0;
                if (snapshot.exists()) {
                    try {
                        partidas = Long.parseLong(snapshot.getValue().toString());
                    } catch (Exception e) {
                        partidas = 0;
                    }
                }
                // Sumamos 1 y subimos el dato
                partidasRef.setValue(partidas + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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

    public void guardarPuntosAcumulados(int puntosPartida, int saltosPartida) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference usuarioRef = baseDatosRef.child(user.getUid());

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 1. ACUMULAR PUNTUACIÓN TOTAL
                long totalPuntos = 0;
                if (snapshot.child("puntuacion_total").exists()) {
                    try {
                        totalPuntos = Long.parseLong(snapshot.child("puntuacion_total").getValue().toString());
                    } catch (Exception e) {}
                }
                usuarioRef.child("puntuacion_total").setValue(totalPuntos + puntosPartida);

                // 2. ACUMULAR SALTOS TOTALES
                long totalSaltos = 0;
                if (snapshot.child("saltos_totales").exists()) {
                    try {
                        totalSaltos = Long.parseLong(snapshot.child("saltos_totales").getValue().toString());
                    } catch (Exception e) {}
                }
                usuarioRef.child("saltos_totales").setValue(totalSaltos + saltosPartida);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // --- NUEVO MÉTODO PARA GUARDAR SALTOS ---
    public void guardarEstadisticas(int saltosDeEstaPartida) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            // Referencia a la base de datos: jugadores -> UID
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("jugadores").child(uid);

            // Leemos los saltos que ya tenía para sumarle los nuevos
            ref.child("saltos").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long saltosTotales = 0;

                    // Si ya había saltos guardados, los recuperamos
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        try {
                            // Convertimos a número (seguro)
                            saltosTotales = Long.parseLong(snapshot.getValue().toString());
                        } catch (Exception e) {
                            saltosTotales = 0;
                        }
                    }

                    // Sumamos los de ahora
                    long nuevosTotales = saltosTotales + saltosDeEstaPartida;

                    // Guardamos el nuevo total en la nube
                    ref.child("saltos").setValue(nuevosTotales);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Si falla, no hacemos nada
                }
            });
        }
    }
}