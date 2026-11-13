package rpg.modelo.unidad;

import rpg.modelo.habilidad.DisparoCertero;


public class Militar extends Personaje {

    public Militar(String nombre) {
        super(nombre, 95, 30, 17, 5, 13);
        crecimientoVida = 9;
        crecimientoMana = 4;
        crecimientoAtaque = 3;
        habilidades.add(new DisparoCertero());
    }

    @Override
    public void atacar(Unidad obj) {
        golpear(obj);
    }

    @Override
    public TipoUnidad getTipo(){
        return TipoUnidad.MILITAR;
    }
}
