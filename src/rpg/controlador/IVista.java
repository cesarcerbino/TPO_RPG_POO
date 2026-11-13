package rpg.controlador;

import java.util.List;

import rpg.dto.BatallaDTO;
import rpg.dto.EquipamientoDTO;
import rpg.dto.PartidaDTO;
import rpg.dto.PartidaGuardadaDTO;
import rpg.dto.ResultadoBatallaDTO;
import rpg.dto.UnidadDTO;

/**
 * Lo que el controlador le puede pedir a CUALQUIER vista. La implementación actual es Swing (VistaSwing),
 * pero el controlador solo conoce esta interfaz y los DTO.
 */
public interface IVista {

    void mostrarMenuPrincipal();

    void mostrarSeleccionPersonajes(List<UnidadDTO> disponibles, int minimo, int maximo);

    /** Es el turno de un personaje del jugador: la vista muestra el estado y espera que elija una acción. */
    void mostrarBatalla(BatallaDTO batalla);

    /**
     * Le toca a un enemigo (batalla.unidadEnTurno()): llega antes de que actúe, para que la vista
     * pueda mostrar quién ataca y separar cada turno enemigo del siguiente.
     */
    void mostrarTurnoEnemigo(BatallaDTO batalla);

    /** Estado final de la batalla, antes del resultado: deja ver el último golpe. */
    void mostrarFinDeBatalla(BatallaDTO batalla);

    void mostrarResultado(ResultadoBatallaDTO resultado, PartidaDTO partida);

    /** Entre batallas: lo que tiene puesto cada miembro y la mochila. Se vuelve a llamar después de cada onEquipar. */
    void mostrarEquipamiento(EquipamientoDTO equipamiento);

    /** Patrón Observador: llega una línea nueva del registro de combate. */
    void mostrarEvento(String texto);

    /** Empieza una batalla nueva: se borra el registro anterior. */
    void limpiarRegistro();

    void mostrarMensaje(String texto);

    /** La vista deja elegir una de las partidas guardadas y llama a controlador.onCargarPartida(nombre). */
    default void mostrarPartidasGuardadas(List<PartidaGuardadaDTO> partidas) {
    }
}
