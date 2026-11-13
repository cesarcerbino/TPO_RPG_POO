package rpg.modelo.objeto;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.TipoEvento;
import rpg.modelo.generacion.GeneradorAleatorio;
import rpg.modelo.habilidad.Objetivo;
import rpg.modelo.unidad.Unidad;


public class Botiquin extends Objeto {

    private final GeneradorAleatorio rng;

    public Botiquin(GeneradorAleatorio rng, int usos) {
        super("Botiquín", "Recupera entre 10% y 40% de la vida", usos, Objetivo.ALIADO);
        this.rng = rng;
    }

    @Override
    public void usar(Unidad usuario, Unidad obj, Batalla b) {
        if (agotado()) {
            return;
        }
        int porcentaje = rng.entre(10, 40);
        int curado = obj.curar(Math.max(1, obj.getVidaMaxima() * porcentaje / 100));
        b.getRegistro().registrar(TipoEvento.OBJETO,
                "  " + obj.getNombre() + " recupera " + curado + " de vida (" + porcentaje + "%).");
        consumir();
    }
}
