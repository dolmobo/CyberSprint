package com.example.cybersprint.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Jugador {

    // DATOS DEL DIAGRAMA
    public String usuario;
    public int monedas;
    public int puntuacion;
    public String skin_equipada;
    public int totalPartidas;
    public int totalSaltos;

    // DATOS GRÁFICOS Y FÍSICA
    public int x, y;
    public int ancho, alto;
    public int velocidadY;
    private Rect rectangulo; // La hitbox

    public Jugador(int startX, int startY) {
        // Inicializar datos
        this.usuario = "Jugador1";
        this.monedas = 0;
        this.puntuacion = 0;

        // Inicializar física
        this.x = startX;
        this.y = startY;
        this.ancho = 100;
        this.alto = 100;
        this.rectangulo = new Rect(x, y, x + ancho, y + alto);
    }

    public void update() {
        // Actualizamos la caja de colisión para que siga al dibujo
        rectangulo.set(x, y, x + ancho, y + alto);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.parseColor("#00FF00")); // Verde Neón
        canvas.drawRect(rectangulo, paint);
    }

    public void setSaltar() {
        this.totalSaltos++;
        // Solo salta si está cerca del suelo (aprox 750px)
        if (this.y >= 750) {
            this.velocidadY = -40; // Impulso hacia arriba
        }
    }

    // --- ESTE ES EL MÉTODO QUE TE FALTABA PARA EL ERROR ---
    public Rect getRectangulo() {
        return this.rectangulo;
    }
}