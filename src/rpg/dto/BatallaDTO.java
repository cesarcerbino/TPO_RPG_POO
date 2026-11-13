package rpg.dto;

import java.util.List;


/** unidadEnTurno es null cuando la batalla ya terminó. */
public record BatallaDTO(
        int nivel,
        int turno,
        UnidadDTO unidadEnTurno,
        List<UnidadDTO> equipo,
        List<UnidadDTO> enemigos,
        List<ObjetoDTO> objetos,
        List<UnidadDTO> proximosTurnos) {
}
