package rpg.modelo.partida;


public class EntreBatallas extends EstadoPartida {

    @Override
    public void actualizar(Partida p) {

    }

    @Override
    public boolean permiteGuardar() {
        return true;
    }

    @Override
    public String getNombre() {
        return "Entre batallas";
    }
}
