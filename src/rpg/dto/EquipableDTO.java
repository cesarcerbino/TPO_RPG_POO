package rpg.dto;

/** ranura es el nombre de la ranura ("ARMA", "ARMADURA"): la vista no conoce el enum del modelo. */
public record EquipableDTO(int indice, String nombre, String descripcion, String ranura,
                           int bonusAtaque, int bonusDefensa) {
}
