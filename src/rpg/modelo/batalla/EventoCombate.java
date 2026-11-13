package rpg.modelo.batalla;

import java.io.Serializable;


public class EventoCombate implements Serializable {

    private final TipoEvento tipo;
    private final String texto;

    public EventoCombate(TipoEvento tipo, String texto) {
        this.tipo = tipo;
        this.texto = texto;
    }

    public TipoEvento getTipo() { return tipo; }
    public String getTexto() { return texto; }
}
