package rpg.modelo.unidad;

import rpg.modelo.generacion.GeneradorAleatorio;
import java.util.List;

public abstract class Zombie extends Enemigo {

    protected int putrefaccion;

    protected Zombie(String nombre, int vida, int ataque, int defensa, int velocidad,
                     int experiencia, int putrefaccion, GeneradorAleatorio rng) {
        super(nombre, vida, ataque, defensa, velocidad, experiencia, rng);
        this.putrefaccion = putrefaccion;
    }

    @Override
    public void atacar(Unidad obj) {
        golpear(obj);
    }

    @Override
    public List<String> alComenzarTurno() {
        List<String> log = super.alComenzarTurno();
        int danio = this.pudrirse();
        if(danio > 0 ){
            log.add(getNombre() + " pierde " + danio + " de vida por putrefacción.");
        }
        return log;
    }

    public int pudrirse() {
        int dano = Math.min(putrefaccion, vida - 1);
        if (dano > 0) {
            recibirDano(dano);
            return dano;
        }
        return 0;
    }
}
