package rpg.modelo.accion;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.TipoEvento;
import rpg.modelo.objeto.Objeto;
import rpg.modelo.unidad.Unidad;


public class UsarObjetoAccion extends Accion {

    private final Objeto objeto;

    public UsarObjetoAccion(Objeto objeto, Unidad objetivo) {
        super("Usar objeto", objetivo);
        this.objeto = objeto;
    }

    @Override
    public void validar(Unidad lanzador, Batalla b){
        if (!lanzador.getBando().esControladoPorJugador()) {
            throw new AccionInvalidaException(lanzador.getNombre() + " no puede usar el inventario del equipo.");
        }
        if (objeto == null || !b.getEquipo().getInventario().getDisponibles().contains(objeto)) {
            throw new AccionInvalidaException("Ese objeto no está disponible en el inventario.");
        }
        validarObjetivo(lanzador, objeto.getTipoObjetivo(), b);
    }
    @Override
    public void ejecutar(Unidad lanzador, Unidad obj, Batalla b) {
        boolean pideObjetivo = objeto.getTipoObjetivo().requiereSeleccion();
        b.getRegistro().registrar(TipoEvento.OBJETO, lanzador.getNombre() + " usa " + objeto.getNombre() + (pideObjetivo ? " sobre " + obj.getNombre() + "." : "."));
        lanzador.usarObjeto(objeto, obj, b);
        if (objeto.agotado()) {
            b.getEquipo().getInventario().quitar(objeto);
        }
    }

    public Objeto getObjeto() {
        return objeto;
    }
}
