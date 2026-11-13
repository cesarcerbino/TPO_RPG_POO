package rpg.modelo.habilidad;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.unidad.Unidad;


public class DisparoCertero extends Habilidad {

    public DisparoCertero() {
        super("Disparo certero", "Daño alto que ignora la defensa", 8, Objetivo.ENEMIGO);
    }

    @Override
    public void ejecutar(Unidad lanzador, Unidad obj, Batalla b) {
        int dano = (int) Math.round(lanzador.getAtaque() * 1.3 * potencia());
        danear(b, obj, Math.max(1, dano));
    }
}
