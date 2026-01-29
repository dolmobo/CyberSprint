package com.example.cybersprint.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.example.cybersprint.R;

public class Obstaculo {
    public int x, y;
    public int ancho, alto;
    private Bitmap sprite;
    public int tipo;

    public Obstaculo(Context context, int screenX, int groundY, int tipo) {
        this.x = screenX;
        this.tipo = tipo;
        this.ancho = 100;

        int idImagen;

        if (tipo == 1) {
            // TIPO 1: BAJO (Salto simple)
            this.alto = 120;
            // CORREGIDO: Usa tu imagen "box.png"
            idImagen = R.drawable.box;

        } else {
            // TIPO 2: ALTO (Torre - Doble Salto)
            this.alto = 250;
            // CORREGIDO: Usa tu imagen "torre.png"
            idImagen = R.drawable.machine;
        }

        this.y = groundY + 200 - this.alto;

        Bitmap original = BitmapFactory.decodeResource(context.getResources(), idImagen);

        if (original != null) {
            sprite = Bitmap.createScaledBitmap(original, ancho, alto, false);
        }
    }

    // --- IMPORTANTE: Añade este método si te faltaba ---
    public void update(double velocidad) {
        x -= velocidad;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (sprite != null) {
            canvas.drawBitmap(sprite, x, y, null);
        } else {
            if (this.tipo == 1) paint.setColor(0xFF00FF00);
            else paint.setColor(0xFFFF0000);
            canvas.drawRect(x, y, x + ancho, y + alto, paint);
        }
    }

    public Rect getHitbox() {
        return new Rect(x + 10, y + 10, x + ancho - 10, y + alto - 10);
    }
}