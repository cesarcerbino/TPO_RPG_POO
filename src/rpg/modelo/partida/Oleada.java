package rpg.modelo.partida;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rpg.modelo.unidad.Enemigo;

public class Oleada implements Serializable {

    private final List<Enemigo> enemigos;

    public Oleada(List<Enemigo> enemigos) {
        this.enemigos = new ArrayList<>(enemigos);
    }

    public List<Enemigo> getEnemigos() {
        return enemigos;
    }
}
