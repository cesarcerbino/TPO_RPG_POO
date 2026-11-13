package rpg.modelo.habilidad;

import java.io.Serializable;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.TipoEvento;
import rpg.modelo.unidad.Unidad;

public abstract class Habilidad implements Serializable {

    protected String nombre;
    protected String descripcion;
    protected int costoBase;
    protected int nivel = 1;
    protected Objetivo objetivo;

    protected Habilidad(String nombre, String descripcion, int costoBase, Objetivo objetivo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoBase = costoBase;
        this.objetivo = objetivo;
    }

    public abstract void ejecutar(Unidad lanzador, Unidad objetivo, Batalla b);


    public int getCostoMana() {
        return costoBase + 2 * (nivel - 1);
    }


    protected double potencia() {
        return 1 + 0.15 * (nivel - 1);
    }


    protected int danear(Batalla b, Unidad obj, int cantidad) {
        int antes = obj.getVida();
        obj.recibirDano(cantidad);
        int real = antes - obj.getVida();
        b.getRegistro().registrar(TipoEvento.HABILIDAD, "  " + obj.getNombre() + " recibe " + real + " de daño.");
        return real;
    }

    public void setNivel(int nivel) { this.nivel = Math.max(1, nivel); }
    public int getNivel() { return nivel; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public Objetivo getTipoObjetivo() { return objetivo; }
}
