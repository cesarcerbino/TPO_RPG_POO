package rpg.modelo.generacion;

import java.io.Serializable;


/**
 * Dificultad según el nivel. Los niveles no terminan: la cantidad de zombies y las probabilidades tienen
 * techo, pero las estadísticas siguen creciendo, así que tarde o temprano el equipo cae.
 */
public class CurvaDeDificultad implements Serializable {

    // Fijo para que las partidas guardadas antes de los niveles infinitos se sigan pudiendo cargar.
    private static final long serialVersionUID = 280022476105939055L;

    /** La escena muestra hasta 4 zombies, igual que el máximo del equipo. */
    private static final int MAX_ZOMBIES = 4;
    private static final double MAX_PROBABILIDAD_INFECCIOSO = 0.5;
    private static final double MAX_PROBABILIDAD_TANQUE = 0.45;

    public int cantidadZombies(int nivel, GeneradorAleatorio rng) {
        int base = nivel >= 5 ? MAX_ZOMBIES : rng.entre(2, 3) + (nivel >= 3 ? 1 : 0);
        return Math.min(MAX_ZOMBIES, base);
    }


    public double factorEstadisticas(int nivel, GeneradorAleatorio rng) {
        double min = 1 + 0.05 * (nivel - 1);
        double max = 1 + 0.15 * (nivel - 1);
        return rng.decimalEntre(min, max);
    }


    public double factorVida(GeneradorAleatorio rng) {
        return rng.decimalEntre(0.9, 1.1);
    }

    public double probabilidadInfeccioso(int nivel) {
        return Math.min(MAX_PROBABILIDAD_INFECCIOSO, 0.10 + 0.10 * (nivel - 1));
    }

    public double probabilidadTanque(int nivel) {
        return Math.min(MAX_PROBABILIDAD_TANQUE, 0.15 + 0.10 * (nivel - 1));
    }

    public double probabilidadRapido() {
        return 0.35;
    }
}
