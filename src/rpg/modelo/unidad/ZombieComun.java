package rpg.modelo.unidad;

import rpg.modelo.generacion.GeneradorAleatorio;


public class ZombieComun extends Zombie {

    public ZombieComun(double factor, GeneradorAleatorio rng) {
        super("Zombie Común", escalar(60, factor), escalar(10, factor), escalar(3, factor),
                escalar(5, factor), 10, 1, rng);
    }
    @Override
    public TipoUnidad getTipo(){
        return TipoUnidad.ZOMBIE_COMUN;
    }
}
