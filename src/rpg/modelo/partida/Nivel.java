package rpg.modelo.partida;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Nivel implements Serializable {

    private final int numero;
    private final List<Oleada> oleadas;
    private int proxima;

    public Nivel(int numero, List<Oleada> oleadas) {
        this.numero = numero;
        this.oleadas = new ArrayList<>(oleadas);
    }


    public Oleada siguienteOleada() {
        return oleadas.get(proxima++);
    }


    public boolean completado() {
        return proxima >= oleadas.size();
    }

    public int getNumero() {
        return numero;
    }
}
