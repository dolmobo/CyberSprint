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

        // Variable para guardar qué imagen vamos a usar
        int idImagen;

        // --- LÓGICA DE TIPOS Y SELECCIÓN DE IMAGEN ---
        if (tipo == 1) {
            // TIPO 1: BAJO (Salto simple)
            this.alto = 120;

            // CAMBIA "caja" POR EL NOMBRE DE TU IMAGEN PEQUEÑA
            // Si no la tienes aún, usa R.drawable.prueba
            idImagen = R.drawable.box;

        } else {
            // TIPO 2: ALTO (Torre - Requiere Doble Salto)
            this.alto = 320;

            // CAMBIA "muro" POR EL NOMBRE DE TU IMAGEN ALTA
            // Si no la tienes aún, usa R.drawable.logo
            idImagen = R.drawable.torre;
        }

        // Posicionamos el obstáculo pegado al suelo
        this.y = groundY + 200 - this.alto;

        // Cargamos la imagen seleccionada arriba
        Bitmap original = BitmapFactory.decodeResource(context.getResources(), idImagen);

        // Protección: Si la imagen existe, la escalamos. Si no, no hacemos nada (se dibujará el cuadrado de color)
        if (original != null) {
            sprite = Bitmap.createScaledBitmap(original, ancho, alto, false);
        }
    }

    public void update(double velocidad) {
        x -= velocidad;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (sprite != null) {
            canvas.drawBitmap(sprite, x, y, null);
        } else {
            // Si te olvidaste de poner las imágenes en la carpeta,
            // esto dibujará rectángulos de colores para que puedas seguir jugando
            if (this.tipo == 1) paint.setColor(0xFF00FF00); // Verde (Fácil)
            else paint.setColor(0xFFFF0000); // Rojo (Difícil)

            canvas.drawRect(x, y, x + ancho, y + alto, paint);
        }
    }

    public Rect getHitbox() {
        return new Rect(x + 10, y + 10, x + ancho - 10, y + alto - 10);
    }
}