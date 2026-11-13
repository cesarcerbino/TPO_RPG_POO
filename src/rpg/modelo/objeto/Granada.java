package rpg.modelo.objeto;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.TipoEvento;
import rpg.modelo.habilidad.Objetivo;
import rpg.modelo.unidad.CalculadorDeDano;
import rpg.modelo.unidad.Enemigo;
import rpg.modelo.unidad.Unidad;


public class Granada extends Objeto {

    public Granada(int usos) {
        super("Granada", "Daño de un ataque base a todos los enemigos", usos, Objetivo.TODOS_ENEMIGOS);
    }

    @Override
    public void usar(Unidad usuario, Unidad obj, Batalla b) {
        if (agotado()) {
            return;
        }
        for (Enemigo e : b.getEnemigosVivos()) {
            int antes = e.getVida();
            e.recibirDano(CalculadorDeDano.getInstancia().calcular(usuario, e));
            b.getRegistro().registrar(TipoEvento.OBJETO, "  " + e.getNombre() + " recibe " + (antes - e.getVida()) + " de daño.");
        }
        consumir();
    }
}
