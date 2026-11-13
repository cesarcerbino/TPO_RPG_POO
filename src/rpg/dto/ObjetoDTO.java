package rpg.dto;


public record ObjetoDTO(int indice, String nombre, String descripcion, int usosRestantes,
                        boolean paraAliados, boolean requiereObjetivo) {
}
