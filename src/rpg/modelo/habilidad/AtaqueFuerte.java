package rpg.modelo.habilidad;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.unidad.CalculadorDeDano;
import rpg.modelo.unidad.Unidad;


public class AtaqueFuerte extends Habilidad {

    private final double multiplicador;

    public AtaqueFuerte(String nombre, double multiplicador, int costoMana) {
        super(nombre, "Ataque con daño x" + multiplicador, costoMana, Objetivo.ENEMIGO);
        this.multiplicador = multiplicador;
    }

    @Override
    public void ejecutar(Unidad lanzador, Unidad obj, Batalla b) {
        int dano = (int) Math.round(CalculadorDeDano.getInstancia().calcular(lanzador, obj) * multiplicador * potencia());
        danear(b, obj, Math.max(1, dano));
    }
}
