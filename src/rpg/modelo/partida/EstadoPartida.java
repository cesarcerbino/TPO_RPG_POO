package rpg.modelo.partida;

import java.io.Serializable;


public abstract class EstadoPartida implements Serializable {

    public abstract void actualizar(Partida p);

    public boolean permiteGuardar() {
        return false;
    }

    public boolean estaEnBatalla(){ return false; }
    public boolean estaTerminada(){ return false; }

    /** Nombre del estado para mostrar (no depende del nombre de la clase Java). */
    public abstract String getNombre();
}
