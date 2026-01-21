package com.example.cybersprint.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.cybersprint.data.GestorDatos; // <--- IMPORTANTE: Importar el gestor de datos
import com.example.cybersprint.game.Jugador;
import com.example.cybersprint.game.Obstaculo;
import com.example.cybersprint.game.Partida;
import com.example.cybersprint.data.GestorDatos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    // Motor
    private Thread gameThread;
    private boolean isPlaying;
    private SurfaceHolder surfaceHolder;
    private Paint paint;

    // Objetos del juego
    private Jugador jugador;
    private Partida partida;
    private ArrayList<Obstaculo> listaObstaculos;

    // Instancia de nuestra escena de Game Over
    private EscenaGameOver escenaGameOver;

    // Procedural
    private long tiempoUltimaAparicion;
    private int tiempoParaProximoObstaculo;
    private Random generadorRandom;

    public GameView(Context context) {
        super(context);
        surfaceHolder = getHolder();
        paint = new Paint();

        jugador = new Jugador(100, 500);
        partida = new Partida(jugador);
        partida.start();

        listaObstaculos = new ArrayList<>();
        generadorRandom = new Random();

        // Inicializamos la clase auxiliar de UI
        escenaGameOver = new EscenaGameOver();

        tiempoUltimaAparicion = System.currentTimeMillis();
        calcularProximoTiempo();
    }

    private void calcularProximoTiempo() {
        tiempoParaProximoObstaculo = 1500 + generadorRandom.nextInt(2000);
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        if (partida.gameState.equals("JUGANDO")) {
            // Física
            jugador.velocidadY += 2;
            jugador.y += jugador.velocidadY;
            if (jugador.y > 800) { jugador.y = 800; jugador.velocidadY = 0; }
            jugador.update();

            // Puntuación y Dificultad
            jugador.puntuacion++;
            if (jugador.puntuacion % 500 == 0) partida.velocidadObstaculo += 2.0;

            // Generar Obstáculos
            if (System.currentTimeMillis() - tiempoUltimaAparicion > tiempoParaProximoObstaculo) {
                listaObstaculos.add(new Obstaculo(2200, 820));
                tiempoUltimaAparicion = System.currentTimeMillis();
                calcularProximoTiempo();
            }

            Iterator<Obstaculo> iterador = listaObstaculos.iterator();
            while (iterador.hasNext()) {
                Obstaculo obs = iterador.next();
                obs.update(partida.velocidadObstaculo);
                if (obs.x < -100) iterador.remove();

                // --- AQUÍ ESTÁ LA MAGIA DE FIREBASE ---
                if (partida.checkColision(obs)) {
                    partida.gameState = "GAMEOVER";

                    // 1. Guardar Récord en la nube
                    GestorDatos.getInstance(getContext()).guardarNuevoRecord(jugador.puntuacion);

                    // 2. Guardar Monedas en la nube
                    int monedasGanadas = jugador.puntuacion / 10;
                    GestorDatos.getInstance(getContext()).sumarMonedas(monedasGanadas);
                }
            }
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.parseColor("#1A1A2E")); // Fondo

            // Dibujar Juego
            paint.setColor(Color.GRAY);
            canvas.drawRect(0, 900, 2500, 1080, paint); // Suelo
            jugador.draw(canvas, paint);
            for (Obstaculo obs : listaObstaculos) obs.draw(canvas, paint);

            // HUD Puntuación (Durante el juego)
            if (partida.gameState.equals("JUGANDO")) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(60);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawText("SCORE: " + jugador.puntuacion, 50, 100, paint);
            }

            // --- GAME OVER CON DATOS REALES ---
            if (partida.gameState.equals("GAMEOVER")) {
                // Recuperamos los datos guardados
                int recordGuardado = GestorDatos.getInstance(getContext()).obtenerRecord();
                int totalMonedas = GestorDatos.getInstance(getContext()).obtenerMonedas();
                int monedasGanadasAhora = jugador.puntuacion / 10;

                // Pasamos TODOS los datos a la escena
                escenaGameOver.dibujar(canvas, paint, jugador.puntuacion, recordGuardado, monedasGanadasAhora, totalMonedas);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (partida.gameState.equals("JUGANDO")) {
                partida.jump();
            }
            else if (partida.gameState.equals("GAMEOVER")) {
                // Le preguntamos a la escena qué botón se tocó
                int accion = escenaGameOver.detectarToque((int)event.getX(), (int)event.getY());

                if (accion == EscenaGameOver.ACCION_JUGAR) {
                    reiniciarJuego();
                } else if (accion == EscenaGameOver.ACCION_VOLVER) {
                    if (getContext() instanceof Activity) ((Activity) getContext()).finish();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void reiniciarJuego() {
        listaObstaculos.clear();
        jugador.y = 500;
        jugador.velocidadY = 0;
        jugador.puntuacion = 0;
        partida.velocidadObstaculo = 15.0;
        partida.start();
        tiempoUltimaAparicion = System.currentTimeMillis();
    }

    private void control() {
        try { Thread.sleep(17); } catch (InterruptedException e) { e.printStackTrace(); }
    }
    public void pause() {
        isPlaying = false;
        try { if (gameThread != null) gameThread.join(); } catch (InterruptedException e) {}
    }
    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}