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
    public int saltosDisponibles;

    private Bitmap sprite;
    private Context context; // Guardamos el contexto para cargar recursos después

    public Jugador(Context context, int startX, int startY, int idSkinEquipada) {
        this.context = context;
        this.x = startX;
        this.y = startY;
        this.velocidadY = 0;
        this.puntuacion = 0;
        this.ancho = 75;
        this.alto = 200;
        this.enSuelo = true;
        this.saltosDisponibles = 2;

        // Cargar la skin inicial
        cargarSkin(idSkinEquipada);
    }

    // --- NUEVO MÉTODO PARA CAMBIAR SKIN EN CALIENTE ---
    public void cargarSkin(int idSkin) {
        int resourceId;
        if (idSkin == 2) {
            resourceId = R.drawable.skin_2;
        } else if (idSkin == 3) {
            resourceId = R.drawable.skin_3;
        } else {
            resourceId = R.drawable.skin_default;
        }

        Bitmap original = BitmapFactory.decodeResource(context.getResources(), resourceId);
        if (original != null) {
            sprite = Bitmap.createScaledBitmap(original, ancho, alto, false);
        }
    }

    public void setSaltar() {
        this.velocidadY = -30;
        this.enSuelo = false;
        if (this.saltosDisponibles > 0) {
            this.saltosDisponibles--;
        }
    }

    public void recargarSaltos() {
        this.saltosDisponibles = 2;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (sprite != null) {
            canvas.drawBitmap(sprite, x, y, null);
        } else {
            canvas.drawRect(x, y, x + ancho, y + alto, paint);
        }
    }

    public Rect getHitbox() {
        return new Rect(x, y, x + ancho, y + alto);
    }
}