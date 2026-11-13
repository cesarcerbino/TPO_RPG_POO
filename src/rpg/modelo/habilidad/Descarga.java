package rpg.modelo.habilidad;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.unidad.Enemigo;
import rpg.modelo.unidad.Unidad;


public class Descarga extends Habilidad {

    public Descarga() {
        super("Descarga eléctrica", "Daña a todos los enemigos", 14, Objetivo.TODOS_ENEMIGOS);
    }

    @Override
    public void ejecutar(Unidad lanzador, Unidad obj, Batalla b) {
        int dano = (int) Math.round(lanzador.getAtaque() * 1.4 * potencia());
        for (Enemigo e : b.getEnemigosVivos()) {
            danear(b, e, Math.max(1, dano));
        }
    }
}
