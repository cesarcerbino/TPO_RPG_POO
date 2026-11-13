package rpg.modelo.objeto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import rpg.modelo.equipamiento.Equipable;

public class Inventario implements Serializable {

    private final List<Objeto> objetos = new ArrayList<>();
    private final List<Equipable> equipables = new ArrayList<>();

    public void agregar(Objeto o) {
        objetos.add(o);
    }

    public void quitar(Objeto o) {
        objetos.remove(o);
    }

    /** Objetos que todavía tienen usos. */
    public List<Objeto> getDisponibles() {
        List<Objeto> disponibles = new ArrayList<>();
        for (Objeto o : objetos) {
            if (!o.agotado()) {
                disponibles.add(o);
            }
        }
        return disponibles;
    }
    public void agregarEquipable(Equipable e) {
        equipables.add(e);
    }

    public void quitarEquipable(Equipable e) {
        equipables.remove(e);
    }

    /** Equipables guardados en la mochila (los que nadie tiene puestos). */
    public List<Equipable> getEquipables() {
        return new ArrayList<>(equipables);
    }

}
