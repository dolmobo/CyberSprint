package com.example.cybersprint.game;

public class Partida {

    public String gameState; // "JUGANDO", "GAMEOVER", "PAUSA"
    public Double velocidadObstaculo;

    private Jugador jugador;

    public Partida(Jugador jugador) {
        this.jugador = jugador;
        this.gameState = "PAUSA";
        this.velocidadObstaculo = 15.0; // Velocidad del juego
    }

    public void start() {
        this.gameState = "JUGANDO";
    }

    public Boolean jump() {
        if (this.gameState.equals("JUGANDO")) {
            jugador.setSaltar();
            return true;
        }
        return false;
    }

    // --- AQUÍ ESTABA EL ERROR: AHORA RECIBE UN OBSTÁCULO ---
    public Boolean checkColision(Obstaculo obstaculo) {
        // Usamos el método getRectangulo() que añadimos al Jugador
        // y el método chocaCon() que tiene el Obstáculo
        return obstaculo.chocaCon(jugador.getRectangulo());
    }
}