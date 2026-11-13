package rpg.modelo.unidad;

import rpg.modelo.habilidad.AtaqueFuerte;


public class Bombero extends Personaje {

    public Bombero(String nombre) {
        super(nombre, 130, 20, 16, 10, 8);
        crecimientoVida = 14;
        crecimientoMana = 3;
        crecimientoAtaque = 2;
        habilidades.add(new AtaqueFuerte("Hachazo", 1.5, 8));
    }

    @Override
    public void atacar(Unidad obj) {
        golpear(obj);
    }

    @Override
    public TipoUnidad getTipo(){
        return TipoUnidad.BOMBERO;
    }
}
