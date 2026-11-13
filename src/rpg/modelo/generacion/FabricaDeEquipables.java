package rpg.modelo.generacion;

import java.io.Serializable;

import rpg.modelo.equipamiento.Equipable;
import rpg.modelo.equipamiento.Ranura;


public class FabricaDeEquipables implements Serializable {

    private static final int CALIDAD_MAXIMA = 4;

    private final GeneradorAleatorio rng;

    public FabricaDeEquipables(GeneradorAleatorio rng) {
        this.rng = rng;
    }

    /** Arma o armadura al azar. Cada nivel desbloquea una pieza mejor, hasta el nivel 5. */
    public Equipable crear(int nivel) {
        int calidad = rng.entre(0, Math.min(CALIDAD_MAXIMA, nivel - 1));
        return rng.probabilidad(0.5) ? crearArma(calidad) : crearArmadura(calidad);
    }

    private Equipable crearArma(int calidad) {
        switch (calidad) {
            case 0:  return new Equipable("Manopla", "Puño reforzado con metal", Ranura.ARMA, 2, 0);
            case 1:  return new Equipable("Machete", "Corta lo que se le cruce", Ranura.ARMA, 3, 0);
            case 2:  return new Equipable("Hacha de incendios", "Pesada pero confiable", Ranura.ARMA, 4, 0);
            case 3:  return new Equipable("Escopeta recortada", "Hay que acercarse para disparar", Ranura.ARMA, 6, -2);
            default: return new Equipable("Motosierra", "Destroza todo, pero deja al descubierto", Ranura.ARMA, 8, -4);
        }
    }

    private Equipable crearArmadura(int calidad) {
        switch (calidad) {
            case 0:  return new Equipable("Campera de cuero", "Protege de los mordiscos", Ranura.ARMADURA, 0, 3);
            case 1:  return new Equipable("Protecciones de moto", "Rodilleras y coderas", Ranura.ARMADURA, 0, 5);
            case 2:  return new Equipable("Traje de bombero", "Grueso y resistente", Ranura.ARMADURA, 0, 7);
            case 3:  return new Equipable("Chaleco antibalas", "Para lo que venga", Ranura.ARMADURA, 0, 9);
            default: return new Equipable("Equipo antidisturbios", "Casi impenetrable, pero limita los movimientos", Ranura.ARMADURA, -1, 12);
        }
    }
}