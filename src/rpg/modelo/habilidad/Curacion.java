package rpg.modelo.habilidad;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.TipoEvento;
import rpg.modelo.unidad.Unidad;

public class Curacion extends Habilidad {

    private static final int CURA_BASE = 30;

    public Curacion() {
        super("Curación", "Restaura vida a un aliado", 10, Objetivo.ALIADO);
    }

    @Override
    public void ejecutar(Unidad lanzador, Unidad obj, Batalla b) {
        int curado = obj.curar((int) Math.round(CURA_BASE * potencia()));
        b.getRegistro().registrar(TipoEvento.HABILIDAD, "  " + obj.getNombre() + " recupera " + curado + " de vida.");
    }
}
