package rpg.modelo.objeto;

import java.io.Serializable;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.habilidad.Objetivo;
import rpg.modelo.unidad.Unidad;

public abstract class Objeto implements Serializable {

    protected String nombre;
    protected String descripcion;
    protected int usosRestantes;
    protected Objetivo objetivo;

    protected Objeto(String nombre, String descripcion, int usosRestantes, Objetivo objetivo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.usosRestantes = usosRestantes;
        this.objetivo = objetivo;
    }


    public abstract void usar(Unidad usuario, Unidad objetivo, Batalla b);

    protected void consumir() {
        if (usosRestantes > 0) {
            usosRestantes--;
        }
    }

    public boolean agotado() {
        return usosRestantes <= 0;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getUsosRestantes() { return usosRestantes; }
    public Objetivo getTipoObjetivo() { return objetivo; }
}
