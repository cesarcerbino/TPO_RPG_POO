package rpg.modelo.partida;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.ResultadoBatalla;
import rpg.modelo.generacion.FabricaDeEnemigos;
import rpg.modelo.objeto.Objeto;
import rpg.modelo.equipamiento.Equipable;
import rpg.modelo.generacion.FabricaDeEquipables;

/**
 * Partida sin fin: los niveles se generan a medida que se avanza y la dificultad sigue subiendo
 * hasta que el equipo cae. Es el único final posible.
 */
public class Partida implements Serializable {

    // Fijo para que las partidas guardadas antes de los niveles infinitos se sigan pudiendo cargar.
    private static final long serialVersionUID = -961994061962242376L;

    public static final int OLEADAS_POR_NIVEL = 2;
    private static final double PROBABILIDAD_BOTIN = 0.5;
    private static final double PROBABILIDAD_EQUIPABLE = 0.35;

    private final FabricaDeEnemigos fabrica;
    private final FabricaDeEquipables fabricaDeEquipables;
    private Equipo equipo;
    private Nivel nivelActual;
    private EstadoPartida estado;
    private Batalla batallaActual;

    public Partida(FabricaDeEnemigos fabrica) {
        this.fabrica = fabrica;
        this.fabricaDeEquipables = new FabricaDeEquipables(fabrica.getRng());
        this.nivelActual = crearNivel(1);
        this.estado = new EnMenu();
    }

    private Nivel crearNivel(int numero) {
        List<Oleada> oleadas = new ArrayList<>();
        for (int i = 0; i < OLEADAS_POR_NIVEL; i++) {
            oleadas.add(fabrica.crearOleada(numero));
        }
        return new Nivel(numero, oleadas);
    }

    public void comenzar(Equipo e) {
        this.equipo = e;
    }


    public Batalla siguienteBatalla() {
        if (terminada()) {
            return null;
        }
        if (nivelActual.completado()) {
            avanzarNivel();
        }
        equipo.descansar();
        Oleada oleada = nivelActual.siguienteOleada();
        batallaActual = new Batalla(equipo, oleada.getEnemigos(), fabrica.getRng());
        setEstado(new EnBatalla());
        return batallaActual;
    }


    public void otorgarRecompensas(ResultadoBatalla r) {
        if (!r.fueVictoria()) {
            return;
        }
        for (String m : equipo.repartirExperiencia(r.getExperiencia())) {
            r.agregarRecompensa(m);
        }
        if (fabrica.getRng().probabilidad(PROBABILIDAD_BOTIN)) {
            Objeto botin = fabrica.crearBotin();
            equipo.getInventario().agregar(botin);
            r.agregarRecompensa("Botín: " + botin.getNombre() + ".");
            r.agregarBotin(botin);
        }
        if (fabrica.getRng().probabilidad(PROBABILIDAD_EQUIPABLE)) {
            Equipable e = fabricaDeEquipables.crear(nivelActual.getNumero());
            equipo.getInventario().agregarEquipable(e);
            r.agregarRecompensa("Equipable: " + e.getNombre() + ".");
            r.agregarEquipable(e);
        }
    }

    public void avanzarNivel() {
        nivelActual = crearNivel(nivelActual.getNumero() + 1);
    }

    public boolean terminada() {
        return estado.estaTerminada();
    }
    public boolean enBatalla() {
        return estado.estaEnBatalla();
    }


    public EstadoPartida getEstado() { return estado; }
    public void setEstado(EstadoPartida estado) { this.estado = estado; }
    public Equipo getEquipo() { return equipo; }
    public Batalla getBatallaActual() { return batallaActual; }
    public Nivel getNivelActual() { return nivelActual; }
}
