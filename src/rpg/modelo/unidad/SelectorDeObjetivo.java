package rpg.modelo.unidad;

import java.io.Serializable;
import java.util.List;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.generacion.GeneradorAleatorio;
import rpg.modelo.partida.Equipo;

public class SelectorDeObjetivo implements Serializable {

    private final GeneradorAleatorio rng;

    public SelectorDeObjetivo(GeneradorAleatorio rng) {
        this.rng = rng;
    }


    public Unidad elegirEnemigo(Batalla b) {
        List<Enemigo> vivos = b.getEnemigosVivos();
        return vivos.isEmpty() ? null : vivos.get(rng.entre(0, vivos.size() - 1));
    }


    public Personaje elegirAliado(Equipo e) {
        List<Personaje> vivos = e.getMiembrosVivos();
        return vivos.isEmpty() ? null : vivos.get(rng.entre(0, vivos.size() - 1));
    }


    public Personaje elegirMasDebil(Equipo e) {
        Personaje debil = null;
        for (Personaje p : e.getMiembrosVivos()) {
            if (debil == null || p.getVida() < debil.getVida()) {
                debil = p;
            }
        }
        return debil;
    }
}
