package rpg.modelo.unidad;

import rpg.modelo.habilidad.Descarga;


public class Ingeniero extends Personaje {

    public Ingeniero(String nombre) {
        super(nombre, 90, 40, 9, 5, 10);
        crecimientoVida = 8;
        crecimientoMana = 6;
        crecimientoAtaque = 2;
        habilidades.add(new Descarga());
    }

    @Override
    public void atacar(Unidad obj) {
        golpear(obj);
    }

    @Override
    public TipoUnidad getTipo(){
        return TipoUnidad.INGENIERO;
    }
}
