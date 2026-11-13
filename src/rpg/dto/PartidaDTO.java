package rpg.dto;

public record PartidaDTO(
        int nivel,
        String estado,
        boolean terminada,
        boolean puedeGuardar) {
}
