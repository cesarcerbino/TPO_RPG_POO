package rpg.modelo.unidad;

public enum Bando {
    JUGADOR,
    ENEMIGO;

    public boolean esControladoPorJugador(){
        return this == JUGADOR;
    }
    public Bando rival(){
        return this == JUGADOR ? ENEMIGO : JUGADOR;
    }
}
