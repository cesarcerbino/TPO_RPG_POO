package rpg.dto;


public record HabilidadDTO(int indice, String nombre, String descripcion, int costoMana,
                           boolean paraAliados, boolean requiereObjetivo) {
}
