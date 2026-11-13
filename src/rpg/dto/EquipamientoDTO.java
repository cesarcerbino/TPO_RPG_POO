package rpg.dto;

import java.util.List;

/** Pantalla de equipamiento: el equipo completo (con lo que tiene puesto cada uno) y la mochila compartida. */
public record EquipamientoDTO(List<UnidadDTO> equipo, List<EquipableDTO> mochila) {
}
