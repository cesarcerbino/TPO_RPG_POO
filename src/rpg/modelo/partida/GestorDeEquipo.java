package rpg.modelo.partida;

import java.util.ArrayList;
import java.util.List;

import rpg.modelo.generacion.GeneradorAleatorio;
import rpg.modelo.objeto.Botiquin;
import rpg.modelo.objeto.Granada;
import rpg.modelo.objeto.Inventario;
import rpg.modelo.objeto.Suero;
import rpg.modelo.objeto.Trampa;
import rpg.modelo.unidad.Bombero;
import rpg.modelo.unidad.Ingeniero;
import rpg.modelo.unidad.Medico;
import rpg.modelo.unidad.Militar;
import rpg.modelo.unidad.Personaje;

public class GestorDeEquipo {

    private final GeneradorAleatorio rng;

    public GestorDeEquipo(GeneradorAleatorio rng) {
        this.rng = rng;
    }


    public List<Personaje> personajesDisponibles() {
        List<Personaje> lista = new ArrayList<>();
        lista.add(new Bombero("Rex"));
        lista.add(new Bombero("Maya"));
        lista.add(new Militar("Cruz"));
        lista.add(new Militar("Ibarra"));
        lista.add(new Ingeniero("Tesla"));
        lista.add(new Medico("Dra. Vega"));
        lista.add(new Medico("Dr. Ruiz"));
        return lista;
    }


    public Equipo crearEquipo(List<Personaje> elegidos) {
        Inventario inv = new Inventario();
        inv.agregar(new Botiquin(rng, 3));
        inv.agregar(new Suero(rng, 2));
        inv.agregar(new Granada(2));
        inv.agregar(new Trampa(1));
        return new Equipo(elegidos, inv);
    }
}
