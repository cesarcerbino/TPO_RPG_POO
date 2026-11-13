package rpg.modelo.accion;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.habilidad.Objetivo;
import rpg.modelo.unidad.Unidad;

public abstract class Accion {

    protected String nombre;

    protected Unidad objetivo;

    protected Accion(String nombre, Unidad objetivo) {
        this.nombre = nombre;
        this.objetivo = objetivo;
    }
    public abstract void validar(Unidad lanzador, Batalla b);

    public abstract void ejecutar(Unidad lanzador, Unidad objetivo, Batalla b);

    protected void validarObjetivo(Unidad lanzador, Objetivo tipo, Batalla b){
        if (!tipo.requiereSeleccion()){
            return;
        }
        if(objetivo == null){
            throw new AccionInvalidaException("Tenés que elegir un objetivo.");
        }
        if (!b.participa(objetivo)){
            throw new AccionInvalidaException(objetivo.getNombre() + "no está en esta batalla.");
        }
        if(!objetivo.estaVivo()){
            throw new AccionInvalidaException(objetivo.getNombre() + "ya cayó: elegí otro objetivo.");
        }
        if (!tipo.admite(lanzador, objetivo)){
            throw new AccionInvalidaException(tipo == Objetivo.ALIADO ? "Tenés que elegir un aliado." : "Tenés que elegir a un enemigo.");

        }
    }

    public String getNombre() { return nombre; }
    public Unidad getObjetivo() { return objetivo; }
}
