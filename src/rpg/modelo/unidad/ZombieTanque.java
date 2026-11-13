package rpg.modelo.unidad;

import rpg.modelo.accion.Accion;
import rpg.modelo.accion.DefenderAccion;
import rpg.modelo.batalla.Batalla;
import rpg.modelo.generacion.GeneradorAleatorio;


public class ZombieTanque extends Zombie {

    public ZombieTanque(double factor, GeneradorAleatorio rng) {
        super("Zombie Tanque", escalar(110, factor), escalar(12, factor), escalar(8, factor),
                escalar(3, factor), 25, 1, rng);
    }

    @Override
    public Accion elegirAccion(Batalla b) {
        if (rng.probabilidad(0.25)) {
            return new DefenderAccion(this);
        }
        return super.elegirAccion(b);
    }
    @Override
    public TipoUnidad getTipo(){
        return TipoUnidad.ZOMBIE_TANQUE;

    }
}
