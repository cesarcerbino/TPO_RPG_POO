package rpg.modelo.partida;

public class Finalizada extends EstadoPartida {

    @Override
    public boolean estaTerminada(){return true;}

    @Override
    public void actualizar(Partida p) {
        // Estado terminal.
    }

    @Override
    public String getNombre() {
        return "Finalizada";
    }
}
