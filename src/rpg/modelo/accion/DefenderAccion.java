package rpg.modelo.accion;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.TipoEvento;
import rpg.modelo.unidad.Unidad;

public class DefenderAccion extends Accion {

    public DefenderAccion(Unidad quienSeDefiende) {
        super("Defender", quienSeDefiende);
    }

    @Override
    public void validar(Unidad lanzador, Batalla b){}

    @Override
    public void ejecutar(Unidad lanzador, Unidad obj, Batalla b) {
        lanzador.alDefender();
        String extra = lanzador.getManaMaximo() > 0 ? " y recupera maná": "";
        b.getRegistro().registrar(TipoEvento.EFECTO,
                lanzador.getNombre() + " se defiende (recibe la mitad de daño hasta su próximo turno)" + extra + ".");
    }
}
