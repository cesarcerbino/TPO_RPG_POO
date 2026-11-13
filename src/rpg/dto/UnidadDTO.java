package rpg.dto;

import java.util.List;


public record UnidadDTO(
        int indice,
        String nombre,
        String tipo,              // nombre visible de la clase ("Médico", "Zombie Tanque")
        String claveVisual,       // identificador estable para elegir el sprite ("MEDICO", "ZOMBIE_TANQUE")
        int vida,
        int vidaMaxima,
        int mana,
        int manaMaximo,
        int nivel,
        int experiencia,
        int experienciaParaSubir,
        int ataque,
        int defensa,
        int velocidad,
        boolean vivo,
        boolean esPersonaje,
        boolean infectado,
        int turnosBloqueado,      // turnos que todavía va a perder (0 = puede actuar)
        boolean defendiendo,
        List<HabilidadDTO> habilidades,
        List<EquipableDTO> equipados) {
}
