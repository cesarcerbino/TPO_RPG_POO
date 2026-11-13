package rpg.modelo.partida;

import rpg.modelo.batalla.Batalla;

public class EnBatalla extends EstadoPartida {


    @Override
    public boolean permiteGuardar() {
        return true;
    }

    @Override
    public boolean estaEnBatalla() {return true;}

    @Override
    public void actualizar(Partida p) {
        Batalla b = p.getBatallaActual();
        if (b == null || !b.finalizada()) {
            return;
        }
        if (p.getEquipo().estaDerrotado()) {
            p.setEstado(new Finalizada());
        } else {
            p.setEstado(new EntreBatallas());
        }
    }

    @Override
    public String getNombre() {
        return "En batalla";
    }
}
