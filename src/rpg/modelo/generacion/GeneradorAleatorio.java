package rpg.modelo.generacion;

import java.io.Serializable;
import java.util.Random;


public class GeneradorAleatorio implements Serializable {

    private final Random random;

    public GeneradorAleatorio() {
        this.random = new Random();
    }

    public GeneradorAleatorio(long semilla) {
        this.random = new Random(semilla);
    }


    public int entre(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }


    public double decimalEntre(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }


    public boolean probabilidad(double p) {
        return random.nextDouble() < p;
    }
}
