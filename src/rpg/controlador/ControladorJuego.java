package rpg.controlador;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import rpg.dto.PartidaGuardadaDTO;
import rpg.modelo.batalla.Batalla;
import rpg.modelo.generacion.FabricaDeEnemigos;
import rpg.modelo.generacion.GeneradorAleatorio;
import rpg.modelo.partida.Equipo;
import rpg.modelo.partida.GestorDeEquipo;
import rpg.modelo.partida.Partida;
import rpg.modelo.unidad.Personaje;

/**
 * Coordinador de la aplicación: navegación entre pantallas y creación de la partida.
 * Delega el flujo de batalla en ControladorBatalla, el equipamiento en ControladorEquipamiento
 * y las partidas guardadas en GestorDePartidasGuardadas.
 * La vista sigue llamando a los mismos métodos onXxx de siempre.
 */
public class ControladorJuego {

    private final IVista vista;
    private final GestorDeEquipo gestor;
    private final FabricaDeEnemigos fabrica;
    private final GestorDePartidasGuardadas guardadas = new GestorDePartidasGuardadas();
    private final ControladorBatalla controladorBatalla;
    private final ControladorEquipamiento controladorEquipamiento;

    private Partida partida;
    private List<Personaje> disponibles = new ArrayList<>();

    public ControladorJuego(IVista vista) {
        this(vista, new GeneradorAleatorio());
    }

    public ControladorJuego(IVista vista, GeneradorAleatorio rng) {
        this.vista = vista;
        this.gestor = new GestorDeEquipo(rng);
        this.fabrica = new FabricaDeEnemigos(rng);
        this.controladorBatalla = new ControladorBatalla(vista);
        this.controladorEquipamiento = new ControladorEquipamiento(vista);
    }

    public void setDirectorioDePartidas(Path directorio) {
        guardadas.setDirectorio(directorio);
    }

    // ------------------------------------------------------------ navegación y creación de partida

    public void iniciar() {
        vista.mostrarMenuPrincipal();
    }

    public void onNuevaPartida() {
        disponibles = gestor.personajesDisponibles();
        vista.mostrarSeleccionPersonajes(ConversorDTO.unidades(disponibles), Equipo.MIN_MIEMBROS, Equipo.MAX_MIEMBROS);
    }

    public void onEquipoElegido(List<Integer> indices) {
        if (indices == null || indices.size() < Equipo.MIN_MIEMBROS || indices.size() > Equipo.MAX_MIEMBROS) {
            vista.mostrarMensaje("Elegí entre " + Equipo.MIN_MIEMBROS + " y " + Equipo.MAX_MIEMBROS + " personajes.");
            return;
        }
        List<Personaje> elegidos = new ArrayList<>();
        for (int i : indices) {
            if (i < 0 || i >= disponibles.size()) {
                vista.mostrarMensaje("Personaje inválido.");
                return;
            }
            elegidos.add(disponibles.get(i));
        }
        partida = new Partida(fabrica);
        partida.comenzar(gestor.crearEquipo(elegidos));
        iniciarSiguienteBatalla();
    }

    public void onSalir() {
        System.exit(0);
    }

    public void onVolverAlMenu() {
        partida = null;
        controladorBatalla.soltar();
        vista.mostrarMenuPrincipal();
    }

    public void onContinuar() {
        if (partida == null || partida.terminada()) {
            vista.mostrarMenuPrincipal();
        } else {
            iniciarSiguienteBatalla();
        }
    }

    private void iniciarSiguienteBatalla() {
        Batalla siguiente = partida.siguienteBatalla();
        if (siguiente == null) {
            vista.mostrarMenuPrincipal();
            return;
        }
        controladorBatalla.jugar(partida, siguiente);
    }

    // ------------------------------------------------------------ batalla (delegado)

    public void onAtacar(int indiceEnemigo) {
        controladorBatalla.onAtacar(indiceEnemigo);
    }

    public void onUsarHabilidad(int indiceHabilidad, int indiceObjetivo) {
        controladorBatalla.onUsarHabilidad(indiceHabilidad, indiceObjetivo);
    }

    public void onUsarObjeto(int indiceObjeto, int indiceObjetivo) {
        controladorBatalla.onUsarObjeto(indiceObjeto, indiceObjetivo);
    }

    public void onDefender() {
        controladorBatalla.onDefender();
    }

    // ------------------------------------------------------------ equipamiento entre batallas (delegado)

    public void onAbrirEquipamiento() {
        controladorEquipamiento.onAbrirEquipamiento(partida);
    }

    public void onEquipar(int indiceMiembro, int indiceEquipable) {
        controladorEquipamiento.onEquipar(partida, indiceMiembro, indiceEquipable);
    }

    // ------------------------------------------------------------ partidas guardadas (delegado)

    public void onGuardar() {
        guardar();
    }

    public void onGuardarYVolverAlMenu() {
        if (guardar()) {
            onVolverAlMenu();
        }
    }

    public void onCargarPartida() {
        mostrarGuardadas();
    }

    public void onCargarPartida(String nombre) {
        try {
            Partida cargada = guardadas.cargar(nombre);
            if (cargada.terminada()) {
                vista.mostrarMensaje("La partida guardada ya había terminado.");
                return;
            }
            partida = cargada;
            Batalla enCurso = partida.getBatallaActual();
            if (partida.enBatalla() && enCurso != null && !enCurso.finalizada()) {
                controladorBatalla.jugar(partida, enCurso);
            } else {
                iniciarSiguienteBatalla();
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            vista.mostrarMensaje("No se pudo cargar la partida: " + e.getMessage());
        }
    }

    public void onBorrarPartida(String nombre) {
        try {
            guardadas.borrar(nombre);
        } catch (IOException e) {
            vista.mostrarMensaje("No se pudo borrar la partida: " + e.getMessage());
            return;
        }
        if (!mostrarGuardadas()) {
            vista.mostrarMenuPrincipal();
        }
    }

    private boolean mostrarGuardadas() {
        List<PartidaGuardadaDTO> lista = guardadas.listar();
        if (lista.isEmpty()) {
            vista.mostrarMensaje("No hay ninguna partida guardada.");
            return false;
        }
        vista.mostrarPartidasGuardadas(lista);
        return true;
    }

    private boolean guardar() {
        if (partida == null || partida.terminada() || !partida.getEstado().permiteGuardar()) {
            vista.mostrarMensaje("No hay una partida en curso que se pueda guardar.");
            return false;
        }
        try {
            String nombre = guardadas.guardar(partida);
            vista.mostrarMensaje("Partida guardada: " + nombre);
            return true;
        } catch (IOException e) {
            vista.mostrarMensaje("No se pudo guardar la partida: " + e.getMessage());
            return false;
        }
    }
}
