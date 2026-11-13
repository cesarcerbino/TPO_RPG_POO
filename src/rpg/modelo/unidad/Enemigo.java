package rpg.modelo.unidad;

import rpg.modelo.accion.Accion;
import rpg.modelo.accion.AtacarAccion;
import rpg.modelo.accion.DefenderAccion;
import rpg.modelo.batalla.Batalla;
import rpg.modelo.generacion.GeneradorAleatorio;

public abstract class Enemigo extends Unidad {

    private final int experienciaQueDa;
    protected final GeneradorAleatorio rng;
    protected final SelectorDeObjetivo selector;

    protected Enemigo(String nombre, int vida, int ataque, int defensa, int velocidad,
                      int experienciaQueDa, GeneradorAleatorio rng) {
        super(nombre, vida, ataque, defensa, velocidad);
        this.experienciaQueDa = experienciaQueDa;
        this.rng = rng;
        this.selector = new SelectorDeObjetivo(rng);
    }

    @Override
    public Bando getBando(){
        return Bando.ENEMIGO;
    }

    protected static int escalar(int base, double factor) {
        return Math.max(1, (int) Math.round(base * factor));
    }


    @Override
    public Accion elegirAccion(Batalla b) {
        Personaje objetivo = elegirObjetivo(b);
        if (objetivo == null) {
            return new DefenderAccion(this);
        }
        return new AtacarAccion(objetivo);
    }

    protected Personaje elegirObjetivo(Batalla b) {
        return selector.elegirAliado(b.getEquipo());
    }


    public void ajustarVida(double factor) {
        this.vidaMaxima = Math.max(1, (int) Math.round(vidaMaxima * factor));
        this.vida = this.vidaMaxima;
    }

    public int getExperiencia() {
        return experienciaQueDa;
    }
}
