package rpg.controlador;

import java.util.List;

import rpg.modelo.equipamiento.Equipable;
import rpg.modelo.partida.Equipo;
import rpg.modelo.partida.Partida;

/**
 * Responsabilidad única: la pantalla de equipamiento (armas y armaduras) entre batallas.
 * Traduce los índices de la vista a miembros y piezas; las reglas las valida el Modelo (Equipo.equipar).
 */
public class ControladorEquipamiento {

    private final IVista vista;

    public ControladorEquipamiento(IVista vista) {
        this.vista = vista;
    }

    public void onAbrirEquipamiento(Partida partida) {
        if (puedeEquipar(partida)) {
            mostrarEquipamiento(partida);
        }
    }

    public void onEquipar(Partida partida, int indiceMiembro, int indiceEquipable) {
        if (!puedeEquipar(partida)) {
            return;
        }
        Equipo equipo = partida.getEquipo();
        List<Equipable> mochila = equipo.getInventario().getEquipables();
        if (indiceMiembro < 0 || indiceMiembro >= equipo.getMiembros().size()
                || indiceEquipable < 0 || indiceEquipable >= mochila.size()) {
            vista.mostrarMensaje("Selección inválida.");
            return;
        }
        try {
            // El modelo valida (miembro vivo, pieza en la mochila); acá solo se muestra el motivo.
            equipo.equipar(equipo.getMiembros().get(indiceMiembro), mochila.get(indiceEquipable));
        } catch (IllegalArgumentException e) {
            vista.mostrarMensaje(e.getMessage());
            return;
        }
        mostrarEquipamiento(partida);
    }

    /** El equipo solo se cambia entre batallas, nunca con una en curso. */
    private boolean puedeEquipar(Partida partida) {
        if (partida == null || partida.terminada() || partida.enBatalla()) {
            vista.mostrarMensaje("El equipo solo se puede cambiar entre batallas.");
            return false;
        }
        return true;
    }

    private void mostrarEquipamiento(Partida partida) {
        vista.mostrarEquipamiento(ConversorDTO.equipamiento(partida.getEquipo()));
    }
}
