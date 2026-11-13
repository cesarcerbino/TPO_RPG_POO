package rpg.modelo.accion;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.TipoEvento;
import rpg.modelo.habilidad.Habilidad;
import rpg.modelo.habilidad.Objetivo;
import rpg.modelo.unidad.Unidad;

public class UsarHabilidadAccion extends Accion {

    private final Habilidad habilidad;

    public UsarHabilidadAccion(Habilidad habilidad, Unidad objetivo) {
        super("Usar habilidad", objetivo);
        this.habilidad = habilidad;
    }

    @Override
    public void validar(Unidad lanzador, Batalla b){
        if (habilidad == null || !lanzador.getHabilidades().contains(habilidad)){
            throw new AccionInvalidaException(lanzador.getNombre() + " no tiene esa habilidad.");
        }
        if (!lanzador.puedePagar(habilidad.getCostoMana())){
            throw new AccionInvalidaException("No alcanza el maná para " + habilidad.getNombre() + " (cuesta " + habilidad.getCostoMana() + ", tiene " + lanzador.getMana() + ").");
        }
        validarObjetivo(lanzador, habilidad.getTipoObjetivo(), b );
    }
    @Override
    public void ejecutar(Unidad lanzador, Unidad obj, Batalla b) {
        lanzador.pagar(habilidad.getCostoMana());
        boolean enArea = habilidad.getTipoObjetivo() == Objetivo.TODOS_ENEMIGOS;
        b.getRegistro().registrar(TipoEvento.HABILIDAD, lanzador.getNombre() + " usa " + habilidad.getNombre() + (enArea ? " sobre todos los enemigos." : obj != null ? " sobre " +  obj.getNombre() + "." : "."));
        lanzador.usarHabilidad(habilidad, obj, b);
    }
    public Habilidad getHabilidad() {
        return habilidad;
    }
}
