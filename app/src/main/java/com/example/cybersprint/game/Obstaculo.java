package com.example.cybersprint.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Obstaculo {

    public int x, y;
    public int ancho, alto;
    private Rect rectangulo;

    public Obstaculo(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.ancho = 80;
        this.alto = 80;
        this.rectangulo = new Rect(x, y, x + ancho, y + alto);
    }

    // Acepta la velocidad como double (que viene de Partida)
    public void update(double velocidadPartida) {
        this.x -= velocidadPartida;
        rectangulo.set(x, y, x + ancho, y + alto);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.RED);
        canvas.drawRect(rectangulo, paint);
    }

    // Comprueba si choca con otro cuadrado (el del jugador)
    public boolean chocaCon(Rect otroRectangulo) {
        return Rect.intersects(this.rectangulo, otroRectangulo);
    }
}