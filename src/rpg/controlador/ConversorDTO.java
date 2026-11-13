package rpg.controlador;

import java.util.ArrayList;
import java.util.List;

import rpg.dto.BatallaDTO;
import rpg.dto.EquipableDTO;
import rpg.dto.EquipamientoDTO;
import rpg.dto.HabilidadDTO;
import rpg.dto.ObjetoDTO;
import rpg.dto.PartidaDTO;
import rpg.dto.ResultadoBatallaDTO;
import rpg.dto.UnidadDTO;
import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.ResultadoBatalla;
import rpg.modelo.equipamiento.Equipable;
import rpg.modelo.habilidad.Habilidad;
import rpg.modelo.objeto.Objeto;
import rpg.modelo.partida.Equipo;
import rpg.modelo.partida.Partida;
import rpg.modelo.unidad.Personaje;
import rpg.modelo.unidad.Unidad;


public final class ConversorDTO {

    /** Cuántos turnos (el actual incluido) se muestran en la barra de orden de la batalla. */
    private static final int TURNOS_A_MOSTRAR = 8;

    private ConversorDTO() {
    }

    public static HabilidadDTO habilidad(Habilidad h, int indice) {
        return new HabilidadDTO(indice, h.getNombre(), h.getDescripcion(), h.getCostoMana(),
                h.getTipoObjetivo().esDeAliados(), h.getTipoObjetivo().requiereSeleccion());
    }

    public static ObjetoDTO objeto(Objeto o, int indice) {
        return new ObjetoDTO(indice, o.getNombre(), o.getDescripcion(), o.getUsosRestantes(),
                o.getTipoObjetivo().esDeAliados(), o.getTipoObjetivo().requiereSeleccion());
    }

    public static EquipableDTO equipable(Equipable e, int indice) {
        return new EquipableDTO(indice, e.getNombre(), e.getDescripcion(), e.getRanura().name(),
                e.getBonusAtaque(), e.getBonusDefensa());
    }

    public static List<EquipableDTO> equipables(List<Equipable> lista) {
        List<EquipableDTO> dtos = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            dtos.add(equipable(lista.get(i), i));
        }
        return dtos;
    }

    /** Los índices de la mochila son los que usa la vista para pedir onEquipar. */
    public static EquipamientoDTO equipamiento(Equipo equipo) {
        return new EquipamientoDTO(unidades(equipo.getMiembros()), equipables(equipo.getInventario().getEquipables()));
    }

    public static UnidadDTO unidad(Unidad u, int indice) {
        List<HabilidadDTO> habs = new ArrayList<>();
        for (int i = 0; i < u.getHabilidades().size(); i++) {
            habs.add(habilidad(u.getHabilidades().get(i), i));
        }
        return new UnidadDTO(indice, u.getNombre(), u.getTipo().getNombreVisible(), u.getTipo().name(),
                u.getVida(), u.getVidaMaxima(), u.getMana(), u.getManaMaximo(),
                u.getNivel(), u.getExperiencia(), u.experienciaParaSubir(),
                u.getAtaque(), u.getDefensa(), u.getVelocidad(),
                u.estaVivo(), u.getBando().esControladoPorJugador(),
                u.isInfectado(), u.getTurnosSalteados(), u.isDefendiendo(), habs,
                equipables(u.getEquipados()));
    }

    public static <T extends Unidad> List<UnidadDTO> unidades(List<T> lista) {
        List<UnidadDTO> dtos = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            dtos.add(unidad(lista.get(i), i));
        }
        return dtos;
    }

    /** Con la batalla terminada no hay unidad en turno: unidadEnTurno queda en null y no hay próximos turnos. */
    public static BatallaDTO batalla(Partida partida, Batalla b) {
        boolean terminada = b.finalizada();
        Unidad enTurno = terminada ? null : b.unidadEnTurno();

        List<ObjetoDTO> objetos = new ArrayList<>();
        List<Objeto> disponibles = b.getEquipo().getInventario().getDisponibles();
        for (int i = 0; i < disponibles.size(); i++) {
            objetos.add(objeto(disponibles.get(i), i));
        }

        List<UnidadDTO> proximos = new ArrayList<>();
        if (!terminada) {
            for (Unidad u : b.proximosTurnos(TURNOS_A_MOSTRAR)) {
                proximos.add(unidad(u, indiceEnBatalla(b, u)));
            }
        }

        return new BatallaDTO(
                partida.getNivelActual().getNumero(),
                b.getTurnoNumero(),
                enTurno == null ? null : unidad(enTurno, indiceEnBatalla(b, enTurno)),
                unidades(b.getEquipo().getMiembros()),
                unidades(b.getEnemigos()),
                objetos,
                proximos);
    }

    /** Índice de la unidad en su bando (equipo o enemigos), el mismo que usa la vista para elegir objetivos. */
    private static int indiceEnBatalla(Batalla b, Unidad u) {
        return b.getUnidadesDe(u.getBando()).indexOf(u);
    }

    /** nivelesAntes: nivel de cada miembro del equipo antes de repartir la experiencia (mismo orden). */
    public static ResultadoBatallaDTO resultado(ResultadoBatalla r, List<Personaje> equipo, List<Integer> nivelesAntes) {
        List<Integer> subidos = new ArrayList<>();
        for (int i = 0; i < equipo.size(); i++) {
            subidos.add(equipo.get(i).getNivel() - nivelesAntes.get(i));
        }
        List<ObjetoDTO> botin = new ArrayList<>();
        for (int i = 0; i < r.getBotin().size(); i++) {
            botin.add(objeto(r.getBotin().get(i), i));
        }
        return new ResultadoBatallaDTO(r.fueVictoria(), r.getTurnosUsados(), r.getExperiencia(),
                unidades(equipo), subidos, botin, equipables(r.getEquipables()));
    }

    public static PartidaDTO partida(Partida p) {
        return new PartidaDTO(p.getNivelActual().getNumero(),
                p.getEstado().getNombre(), p.terminada(),
                !p.terminada() && p.getEstado().permiteGuardar());
    }
}
