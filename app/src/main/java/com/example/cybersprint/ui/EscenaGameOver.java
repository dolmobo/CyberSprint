package com.example.cybersprint.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class EscenaGameOver {

    // Rectángulos de los botones
    private Rect btnRevivir, btnJugar, btnVolver;
    private boolean inicializado = false;

    // Constantes para saber qué botón se tocó
    public static final int ACCION_NINGUNA = 0;
    public static final int ACCION_REVIVIR = 1;
    public static final int ACCION_JUGAR = 2;
    public static final int ACCION_VOLVER = 3;

    // AHORA RECIBE 4 DATOS: Puntuación, Récord, Monedas Ganadas y Monedas Totales
    public void dibujar(Canvas canvas, Paint paint, int puntuacion, int record, int monedasGanadas, int totalMonedas) {
        int ancho = canvas.getWidth();
        int alto = canvas.getHeight();

        if (!inicializado) inicializarBotones(ancho, alto);

        // 1. Fondo y Título
        canvas.drawColor(Color.parseColor("#AA000000"));
        paint.setColor(Color.RED);
        paint.setTextSize(120);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("GAME OVER", ancho * 0.35f, alto / 2, paint);

        // 2. Panel Estadísticas
        float panelIzq = ancho * 0.65f;
        float panelDer = ancho * 0.95f;
        float panelArr = alto * 0.1f;
        float panelAbj = alto * 0.9f;
        float centroPanel = (panelIzq + panelDer) / 2;

        paint.setColor(Color.parseColor("#16213E"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(panelIzq, panelArr, panelDer, panelAbj, paint);

        paint.setColor(Color.CYAN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawRect(panelIzq, panelArr, panelDer, panelAbj, paint);
        paint.setStyle(Paint.Style.FILL);

        // 3. TEXTOS DE ESTADÍSTICAS
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("ESTADÍSTICAS", centroPanel, alto * 0.2f, paint);

        // Puntos
        paint.setTextSize(45);
        paint.setColor(Color.YELLOW);
        canvas.drawText("Puntos: " + puntuacion, centroPanel, alto * 0.3f, paint);

        // Récord
        paint.setColor(Color.GREEN);
        canvas.drawText("Récord: " + record, centroPanel, alto * 0.4f, paint);

        // --- NUEVO: MONEDAS ---
        paint.setColor(Color.parseColor("#FFA500")); // Color Naranja/Dorado
        // Monedas de esta partida
        canvas.drawText("+" + monedasGanadas + " Monedas", centroPanel, alto * 0.5f, paint);
        // Monedas totales en la hucha
        paint.setTextSize(35);
        paint.setColor(Color.LTGRAY);
        canvas.drawText("Total: " + totalMonedas + " $", centroPanel, alto * 0.58f, paint);

        // 4. Botones
        dibujarBoton(canvas, paint, btnRevivir, Color.GRAY, "REVIVIR");
        dibujarBoton(canvas, paint, btnJugar, Color.GREEN, "JUGAR");
        dibujarBoton(canvas, paint, btnVolver, Color.RED, "VOLVER");

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void inicializarBotones(int ancho, int alto) {
        // --- AQUÍ ESTÁ EL CAMBIO PARA CENTRAR ---

        // 1. Límites del panel (coinciden con el dibujo de arriba)
        int panelIzq = (int) (ancho * 0.65);
        int panelDer = (int) (ancho * 0.95);

        // 2. Calculamos el CENTRO EXACTO del panel
        int centroPanel = (panelIzq + panelDer) / 2;

        // 3. Tamaño de los botones
        int btnAncho = 300;
        int btnAlto = 80;
        int mitadBtn = btnAncho / 2;

        // 4. Coordenadas X: Desde el centro restamos la mitad del botón
        int btnIzq = centroPanel - mitadBtn;
        int btnDer = centroPanel + mitadBtn;

        // 5. Crear los rectángulos
        btnRevivir = new Rect(btnIzq, alto / 2 - 50, btnDer, alto / 2 - 50 + btnAlto);
        btnJugar = new Rect(btnIzq, alto / 2 + 50, btnDer, alto / 2 + 50 + btnAlto);
        btnVolver = new Rect(btnIzq, alto / 2 + 150, btnDer, alto / 2 + 150 + btnAlto);

        inicializado = true;
    }

    private void dibujarBoton(Canvas c, Paint p, Rect r, int color, String texto) {
        p.setColor(color);
        c.drawRect(r, p);

        // Configuración texto del botón
        p.setColor(Color.BLACK);
        if (color == Color.RED) p.setColor(Color.WHITE);

        p.setTextSize(40);
        p.setTextAlign(Paint.Align.CENTER); // Centrar texto dentro del botón

        // Math trick para centrar verticalmente el texto
        float textY = r.centerY() - ((p.descent() + p.ascent()) / 2);
        c.drawText(texto, r.centerX(), textY, p);
    }

    public int detectarToque(int x, int y) {
        if (!inicializado) return ACCION_NINGUNA;

        if (btnRevivir.contains(x, y)) return ACCION_REVIVIR;
        if (btnJugar.contains(x, y)) return ACCION_JUGAR;
        if (btnVolver.contains(x, y)) return ACCION_VOLVER;

        return ACCION_NINGUNA;
    }
}