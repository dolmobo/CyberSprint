package com.example.cybersprint.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import com.example.cybersprint.R;
import com.example.cybersprint.data.GestorDatos;
import com.example.cybersprint.game.Jugador;
import com.example.cybersprint.game.Obstaculo;
import com.example.cybersprint.game.Partida;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private boolean isPlaying;
    private Thread thread;
    private SurfaceHolder holder;
    private Paint paint;

    private Jugador jugador;
    private Partida partida;
    private List<Obstaculo> listaObstaculos;

    private Bitmap fondo1, fondo2;
    private int fondoX1, fondoX2;
    private int anchoPantalla, altoPantalla;

    private int tiempoSpawn = 0;
    private int saltosPartida = 0;
    private long mejorRecord = 0;
    private int proximoSpawn = 0;
    private Random generadorRandom;

    private RectF botonReintentar;
    private RectF botonMenu;
    private Typeface fuenteCyber;

    public GameView(Context context, int idSkinSeleccionada) {
        super(context);
        holder = getHolder();
        paint = new Paint();
        generadorRandom = new Random();

        try {
            fuenteCyber = ResourcesCompat.getFont(context, R.font.font_bold);
            paint.setTypeface(fuenteCyber);
        } catch (Exception e) {
            paint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        anchoPantalla = getResources().getDisplayMetrics().widthPixels;
        altoPantalla = getResources().getDisplayMetrics().heightPixels;

        int centroX = anchoPantalla / 2;
        int centroY = altoPantalla / 2;
        int anchoBoton = 250;
        int altoBoton = 100;
        int separacion = 30;

        botonReintentar = new RectF(centroX - anchoBoton - separacion, centroY + 150, centroX - separacion, centroY + 150 + altoBoton);
        botonMenu = new RectF(centroX + separacion, centroY + 150, centroX + anchoBoton + separacion, centroY + 150 + altoBoton);

        Bitmap fondoOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.fondo_cyber);
        if (fondoOriginal == null) {
            fondoOriginal = Bitmap.createBitmap(anchoPantalla, altoPantalla, Bitmap.Config.ARGB_8888);
            fondoOriginal.eraseColor(Color.rgb(20, 20, 40));
        }
        fondo1 = Bitmap.createScaledBitmap(fondoOriginal, anchoPantalla, altoPantalla, false);
        fondo2 = Bitmap.createScaledBitmap(fondoOriginal, anchoPantalla, altoPantalla, false);
        fondoX1 = 0;
        fondoX2 = anchoPantalla;

        // INICIALIZAR JUGADOR
        jugador = new Jugador(context, 100, 850, idSkinSeleccionada);
        partida = new Partida(jugador);
        listaObstaculos = new ArrayList<>();

        calcularProximoSpawn();
        cargarRecordDesdeFirebase();
    }

    // --- MÉTODO CLAVE PARA ACTUALIZAR SKIN SIN REINICIAR ---
    public void actualizarSkinJugador(int nuevaSkinId) {
        if (jugador != null) {
            jugador.cargarSkin(nuevaSkinId);
        }
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
            partida.actualizarDificultad();

            // --- Lógica del Fondo ---
            int velocidadFondo = (int) (partida.velocidadObstaculo * 0.7);
            fondoX1 -= velocidadFondo;
            fondoX2 -= velocidadFondo;
            if (fondoX1 + anchoPantalla < 0) fondoX1 = fondoX2 + anchoPantalla;
            if (fondoX2 + anchoPantalla < 0) fondoX2 = fondoX1 + anchoPantalla;

            // --- Física del Jugador ---
            jugador.velocidadY += 2;
            jugador.y += jugador.velocidadY;

            if (jugador.y > 850) {
                jugador.y = 850;
                jugador.velocidadY = 0;
                jugador.enSuelo = true;
                jugador.recargarSaltos();
            }

            // --- Generación de Obstáculos ---
            tiempoSpawn++;
            if (tiempoSpawn > proximoSpawn) {
                int tipoObstaculo = 1;
                if (generadorRandom.nextInt(100) < 40) tipoObstaculo = 2;

                // Usamos TUS coordenadas originales
                listaObstaculos.add(new Obstaculo(getContext(), anchoPantalla, 850, tipoObstaculo));
                tiempoSpawn = 0;
                calcularProximoSpawn();
            }

            // --- Mover y Colisiones ---
            Iterator<Obstaculo> iterator = listaObstaculos.iterator();
            while (iterator.hasNext()) {
                Obstaculo obs = iterator.next();
                obs.update(partida.velocidadObstaculo);

                if (obs.x + obs.ancho < 0) {
                    iterator.remove();
                    jugador.puntuacion += 10;
                }

                if (partida.checkColision(obs)) {
                    partida.gameState = "GAMEOVER";

                    // Guardamos todo en Firebase
                    GestorDatos.getInstance(getContext()).guardarNuevoRecord(jugador.puntuacion);
                    GestorDatos.getInstance(getContext()).sumarMonedas(jugador.puntuacion / 10);
                    GestorDatos.getInstance(getContext()).guardarEstadisticas(saltosPartida);

                    // --- NUEVO: SUMAR PARTIDA JUGADA ---
                    GestorDatos.getInstance(getContext()).sumarPartidaJugada();
                    GestorDatos.getInstance(getContext()).guardarPuntosAcumulados(jugador.puntuacion, saltosPartida);

                    if (jugador.puntuacion > mejorRecord) mejorRecord = jugador.puntuacion;
                }
            }
        }
    }

    private void calcularProximoSpawn() {
        int baseFrecuencia = 100 - (int) partida.velocidadObstaculo;
        if (baseFrecuencia < 35) baseFrecuencia = 35;
        int caos = generadorRandom.nextInt(60) - 10;
        proximoSpawn = baseFrecuencia + caos;
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            canvas.drawBitmap(fondo1, fondoX1, 0, null);
            canvas.drawBitmap(fondo2, fondoX2, 0, null);
            jugador.draw(canvas, paint);
            for (Obstaculo obs : listaObstaculos) {
                obs.draw(canvas, paint);
            }

            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setShadowLayer(10, 0, 0, Color.BLACK);
            canvas.drawText("Puntos: " + jugador.puntuacion, 50, 100, paint);
            paint.setShadowLayer(0, 0, 0, 0);

            if (partida.gameState.equals("GAMEOVER")) {
                drawGameOver(canvas);
            } else if (partida.gameState.equals("PAUSA")) {
                drawStartScreen(canvas);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawStartScreen(Canvas canvas) {
        paint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(0, 0, anchoPantalla, altoPantalla, paint);
        paint.setColor(Color.CYAN);
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText("TOCA PARA EMPEZAR", anchoPantalla / 2, altoPantalla / 2, paint);
    }

    private void drawGameOver(Canvas canvas) {
        int centroX = anchoPantalla / 2;
        int centroY = altoPantalla / 2;
        paint.setColor(Color.argb(240, 10, 10, 20));
        canvas.drawRect(0, 0, anchoPantalla, altoPantalla, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.RED);
        paint.setTextSize(120);
        paint.setFakeBoldText(true);
        paint.setShadowLayer(20, 0, 0, Color.RED);
        canvas.drawText("GAME OVER", centroX, centroY - 200, paint);
        paint.setShadowLayer(0, 0, 0, 0);
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        paint.setFakeBoldText(false);
        canvas.drawText("Puntos: " + jugador.puntuacion, centroX, centroY - 100, paint);
        paint.setColor(Color.YELLOW);
        paint.setTextSize(50);
        canvas.drawText("Récord: " + mejorRecord, centroX, centroY - 30, paint);
        paint.setColor(Color.CYAN);
        canvas.drawText("Saltos: " + saltosPartida, centroX, centroY + 40, paint);
        paint.setColor(Color.rgb(0, 229, 255));
        canvas.drawRoundRect(botonReintentar, 20, 20, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        canvas.drawText("JUGAR", botonReintentar.centerX(), botonReintentar.centerY() + 15, paint);
        paint.setColor(Color.rgb(200, 50, 50));
        canvas.drawRoundRect(botonMenu, 20, 20, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("MENÚ", botonMenu.centerX(), botonMenu.centerY() + 15, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            if (partida.gameState.equals("JUGANDO")) {
                if (partida.jump()) saltosPartida++;
            }
            else if (partida.gameState.equals("PAUSA")) {
                partida.start();
            }
            else if (partida.gameState.equals("GAMEOVER")) {
                if (botonReintentar.contains(x, y)) reiniciarPartida();
                else if (botonMenu.contains(x, y)) salirAlMenu();
            }
        }
        return super.onTouchEvent(event);
    }

    private void reiniciarPartida() {
        jugador.y = 850;
        jugador.velocidadY = 0;
        jugador.puntuacion = 0;
        jugador.enSuelo = true;
        jugador.recargarSaltos();
        saltosPartida = 0;
        tiempoSpawn = 0;
        listaObstaculos.clear();
        partida.velocidadObstaculo = 25.0;
        partida.start();
        calcularProximoSpawn();
        cargarRecordDesdeFirebase();
    }

    private void salirAlMenu() {
        isPlaying = false;
        Context context = getContext();
        if (context instanceof Activity) {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }

    private void cargarRecordDesdeFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("jugadores").child(uid).child("record");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try { mejorRecord = Long.parseLong(snapshot.getValue().toString()); }
                        catch (Exception e) {}
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        }
    }

    private void control() {
        try { Thread.sleep(17); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    public void pause() {
        isPlaying = false;
        try {
            if (thread != null) { // Protección anti-crash
                thread.join();
            }
        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }
}