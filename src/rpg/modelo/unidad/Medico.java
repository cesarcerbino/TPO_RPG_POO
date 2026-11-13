package rpg.modelo.unidad;

import rpg.modelo.habilidad.Curacion;


public class Medico extends Personaje {

    public Medico(String nombre) {
        super(nombre, 90, 40, 7, 5, 12);
        crecimientoVida = 9;
        crecimientoMana = 6;
        crecimientoAtaque = 1;
        habilidades.add(new Curacion());
    }

    @Override
    public void atacar(Unidad obj) {
        golpear(obj);
    }

    @Override
    public TipoUnidad getTipo(){
        return TipoUnidad.MEDICO;
    }
}
