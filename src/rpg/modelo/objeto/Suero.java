package rpg.modelo.objeto;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.TipoEvento;
import rpg.modelo.generacion.GeneradorAleatorio;
import rpg.modelo.habilidad.Objetivo;
import rpg.modelo.unidad.Unidad;


public class Suero extends Objeto {

    private final GeneradorAleatorio rng;

    public Suero(GeneradorAleatorio rng, int usos) {
        super("Suero", "Cura la infección (o 5%-15% de vida si no hay)", usos, Objetivo.ALIADO);
        this.rng = rng;
    }

    @Override
    public void usar(Unidad usuario, Unidad obj, Batalla b) {
        if (agotado()) {
            return;
        }
        if (obj.isInfectado()) {
            obj.curarInfeccion();
            b.getRegistro().registrar(TipoEvento.OBJETO, "  " + obj.getNombre() + " ya no está infectado.");
        } else {
            int porcentaje = rng.entre(5, 15);
            int curado = obj.curar(Math.max(1, obj.getVidaMaxima() * porcentaje / 100));
            b.getRegistro().registrar(TipoEvento.OBJETO,
                    "  " + obj.getNombre() + " no estaba infectado: recupera " + curado + " de vida (" + porcentaje + "%).");
        }
        consumir();
    }
}
