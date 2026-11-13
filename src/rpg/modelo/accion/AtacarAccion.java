package rpg.modelo.accion;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.TipoEvento;
import rpg.modelo.unidad.Unidad;
import rpg.modelo.habilidad.Objetivo;

public class AtacarAccion extends Accion {

    public AtacarAccion(Unidad objetivo) {
        super("Atacar", objetivo);
    }

    @Override
    public void validar(Unidad lanzador, Batalla b){
        validarObjetivo(lanzador, Objetivo.ENEMIGO, b);
    }

    @Override
    public void ejecutar(Unidad lanzador, Unidad obj, Batalla b) {
        int antes = obj.getVida();
        lanzador.atacar(obj);
        b.getRegistro().registrar(TipoEvento.ATAQUE, lanzador.getNombre() + " ataca a " + obj.getNombre()
                + " y causa " + (antes - obj.getVida()) + " de daño.");
    }
}
