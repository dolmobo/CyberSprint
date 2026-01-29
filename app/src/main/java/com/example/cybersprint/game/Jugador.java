package com.example.cybersprint.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.example.cybersprint.R;

public class Jugador {
    public int x, y;
    public int velocidadY;
    public int ancho, alto;
    public int puntuacion;

    public boolean enSuelo;

    // --- NUEVO: CONTADOR DE SALTOS ---
    public int saltosDisponibles;

    private Bitmap sprite;

    public Jugador(Context context, int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.velocidadY = 0;
        this.puntuacion = 0;
        this.ancho = 100;
        this.alto = 200;
        this.enSuelo = true;

        // Empezamos con 2 saltos
        this.saltosDisponibles = 2;

        Bitmap original = BitmapFactory.decodeResource(context.getResources(), R.drawable.prueba);
        // Si la imagen falla, evitamos el crash (aunque deberías tenerla)
        if (original != null) {
            sprite = Bitmap.createScaledBitmap(original, ancho, alto, false);
        }
    }

    public void setSaltar() {
        this.velocidadY = -30; // Fuerza del salto
        this.enSuelo = false;

        // --- RESTAMOS UN SALTO ---
        if (this.saltosDisponibles > 0) {
            this.saltosDisponibles--;
        }
    }

    // Método para recargar saltos al tocar el suelo
    public void recargarSaltos() {
        this.saltosDisponibles = 2;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (sprite != null) {
            canvas.drawBitmap(sprite, x, y, null);
        } else {
            // Cuadrado de emergencia si no hay imagen
            canvas.drawRect(x, y, x + ancho, y + alto, paint);
        }
    }

    public Rect getHitbox() {
        return new Rect(x, y, x + ancho, y + alto);
    }
}