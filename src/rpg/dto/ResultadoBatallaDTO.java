package rpg.dto;

import java.util.List;

/**
 * Resultado de una batalla ya con las recompensas aplicadas: el equipo completo (vivos y caídos), cuántos
 * niveles subió cada miembro (mismo orden que equipo), los objetos de botín y los equipables encontrados.
 */
public record ResultadoBatallaDTO(
        boolean victoria,
        int turnosUsados,
        int experienciaGanada,
        List<UnidadDTO> equipo,
        List<Integer> nivelesSubidos,
        List<ObjetoDTO> botin,
        List<EquipableDTO> equipables) {
}
