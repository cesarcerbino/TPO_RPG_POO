package rpg.modelo.generacion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rpg.modelo.objeto.Botiquin;
import rpg.modelo.objeto.Granada;
import rpg.modelo.objeto.Objeto;
import rpg.modelo.objeto.Suero;
import rpg.modelo.objeto.Trampa;
import rpg.modelo.partida.Oleada;
import rpg.modelo.unidad.Enemigo;
import rpg.modelo.unidad.ZombieComun;
import rpg.modelo.unidad.ZombieRapido;
import rpg.modelo.unidad.ZombieTanque;


public class FabricaDeEnemigos implements Serializable {

    private final GeneradorAleatorio rng;
    private final CurvaDeDificultad curva = new CurvaDeDificultad();

    public FabricaDeEnemigos(GeneradorAleatorio rng) {
        this.rng = rng;
    }

    public GeneradorAleatorio getRng() {
        return rng;
    }

    public Enemigo crearEnemigo(int nivel) {
        double factor = curva.factorEstadisticas(nivel, rng);
        Enemigo e;
        if (rng.probabilidad(curva.probabilidadTanque(nivel))) {
            e = new ZombieTanque(factor, rng);
        } else if (rng.probabilidad(curva.probabilidadRapido())) {
            e = new ZombieRapido(factor, rng);
        } else {
            e = new ZombieComun(factor, rng);
        }
        e.ajustarVida(curva.factorVida(rng));
        if (rng.probabilidad(curva.probabilidadInfeccioso(nivel))) {
            e.infectar();
        }
        return e;
    }


    public Oleada crearOleada(int nivel) {
        int cantidad = curva.cantidadZombies(nivel, rng);
        List<Enemigo> enemigos = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            Enemigo e = crearEnemigo(nivel);
            e.setNombre(e.getNombre() + " " + (char) ('A' + i));
            enemigos.add(e);
        }
        return new Oleada(enemigos);
    }


    public Objeto crearBotin() {
        switch (rng.entre(0, 3)) {
            case 0:  return new Botiquin(rng, 1);
            case 1:  return new Suero(rng, 1);
            case 2:  return new Granada(1);
            default: return new Trampa(1);
        }
    }
}
