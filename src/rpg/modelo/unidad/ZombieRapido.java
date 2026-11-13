package rpg.modelo.unidad;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.generacion.GeneradorAleatorio;


public class ZombieRapido extends Zombie {

    private static final int BONUS = 6;

    private final int bonusVelocidad = BONUS;

    public ZombieRapido(double factor, GeneradorAleatorio rng) {
        super("Zombie Rápido", escalar(45, factor), escalar(9, factor), escalar(2, factor),
                escalar(8 + BONUS, factor), 15, 1, rng);
    }

    @Override
    protected Personaje elegirObjetivo(Batalla b) {
        return selector.elegirMasDebil(b.getEquipo());
    }

    public int getBonusVelocidad() {
        return bonusVelocidad;
    }

    @Override
    public TipoUnidad getTipo(){
        return TipoUnidad.ZOMBIE_RAPIDO;
    }
}
