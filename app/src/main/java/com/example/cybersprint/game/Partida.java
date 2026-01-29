package com.example.cybersprint.game;

import android.graphics.Rect;

public class Partida {

    public double velocidadObstaculo;
    public String gameState; // "JUGANDO", "GAMEOVER", "PAUSA"
    private Jugador jugador;

    public Partida(Jugador jugador) {
        this.jugador = jugador;
        // --- CAMBIO 1: VELOCIDAD INICIAL ---
        // Antes era 15.0, ahora 25.0 para que empiece rápido
        this.velocidadObstaculo = 25.0;
        this.gameState = "PAUSA";
    }

    public void start() {
        this.gameState = "JUGANDO";
    }

    public boolean jump() {
        if (this.gameState.equals("JUGANDO") && jugador.saltosDisponibles > 0) {
            jugador.setSaltar();
            return true;
        }
        return false;
    }

    public void actualizarDificultad() {
        // --- CAMBIO 2: ACELERACIÓN AGRESIVA ---
        // Aumentamos la velocidad cada 100 puntos (antes era cada 500)
        // Y subimos 0.1 de golpe
        if (jugador.puntuacion > 0 && jugador.puntuacion % 100 == 0) {
            velocidadObstaculo += 0.1;
        }

        // --- CAMBIO 3: LÍMITE MÁXIMO ---
        // Subimos el tope a 60 (antes 35) para que se ponga imposible
        if (velocidadObstaculo > 60) velocidadObstaculo = 60;
    }

    public boolean checkColision(Obstaculo obs) {
        if (Rect.intersects(jugador.getHitbox(), obs.getHitbox())) {
            return true;
        }
        return false;
    }
}