package rpg.modelo.objeto;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.TipoEvento;
import rpg.modelo.habilidad.Objetivo;
import rpg.modelo.unidad.Enemigo;
import rpg.modelo.unidad.Unidad;


public class Trampa extends Objeto {

    public static final int TURNOS_QUE_SALTEA = 2;

    public Trampa(int usos) {
        super("Trampa", "El enemigo más lento pierde 2 turnos", usos, Objetivo.NINGUNO);
    }

    @Override
    public void usar(Unidad usuario, Unidad obj, Batalla b) {
        if (agotado()) {
            return;
        }
        Enemigo masLento = null;
        for (Enemigo e : b.getEnemigosVivos()) {
            if (masLento == null || e.getVelocidad() < masLento.getVelocidad()) {
                masLento = e;
            }
        }
        if (masLento == null) {
            return;
        }
        masLento.saltearTurnos(TURNOS_QUE_SALTEA);
        b.getRegistro().registrar(TipoEvento.OBJETO,
                "  " + masLento.getNombre() + " queda atrapado y perderá " + TURNOS_QUE_SALTEA + " turnos.");
        consumir();
    }
}
