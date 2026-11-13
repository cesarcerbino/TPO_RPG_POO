package rpg.modelo.partida;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rpg.modelo.equipamiento.Equipable;
import rpg.modelo.objeto.Inventario;
import rpg.modelo.unidad.Personaje;

public class Equipo implements Serializable {

    public static final int MIN_MIEMBROS = 2;
    public static final int MAX_MIEMBROS = 4;

    private final List<Personaje> miembros;
    private final Inventario inventario;

    public Equipo(List<Personaje> miembros, Inventario inventario) {
        if (miembros.size() < MIN_MIEMBROS || miembros.size() > MAX_MIEMBROS) {
            throw new IllegalArgumentException(
                    "El equipo debe tener entre " + MIN_MIEMBROS + " y " + MAX_MIEMBROS + " personajes");
        }
        this.miembros = new ArrayList<>(miembros);
        this.inventario = inventario;
    }

    public List<Personaje> getMiembros() {
        return miembros;
    }

    public List<Personaje> getMiembrosVivos() {
        List<Personaje> vivos = new ArrayList<>();
        for (Personaje p : miembros) {
            if (p.estaVivo()) {
                vivos.add(p);
            }
        }
        return vivos;
    }

    public boolean estaDerrotado() {
        return getMiembrosVivos().isEmpty();
    }

    public Inventario getInventario() {
        return inventario;
    }


    public List<String> repartirExperiencia(int xp) {
        List<String> mensajes = new ArrayList<>();
        for (Personaje p : getMiembrosVivos()) {
            mensajes.addAll(p.ganarExperiencia(xp));
        }
        return mensajes;
    }


    public void descansar() {
        for (Personaje p : getMiembrosVivos()) {
            p.restaurarMana();
            p.curar(p.getVidaMaxima() / 4);
        }
    }
    public void equipar(Personaje p, Equipable e) {
        if (!miembros.contains(p) || !p.estaVivo()) {
            throw new IllegalArgumentException("Solo se puede equipar a un miembro vivo del equipo");
        }
        if (!inventario.getEquipables().contains(e)) {
            throw new IllegalArgumentException("El equipable no está en la mochila del equipo");
        }
        inventario.quitarEquipable(e);
        Equipable anterior = p.equipar(e);
        if (anterior != null) {
            inventario.agregarEquipable(anterior);
        }
    }
}
