package rpg.modelo.partida;

public class EnMenu extends EstadoPartida {

    @Override
    public void actualizar(Partida p) {
        // Nada que hacer: la partida todavía no empezó.
    }

    @Override
    public boolean permiteGuardar() {
        return true;
    }

    @Override
    public String getNombre() {
        return "En menú";
    }
}
