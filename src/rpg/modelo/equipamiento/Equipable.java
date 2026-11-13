package rpg.modelo.equipamiento;

import java.io.Serializable;

/** Arma o armadura que suma (o resta) ataque y defensa mientras el personaje la tiene puesta. */
public class Equipable implements Serializable {

    private final String nombre;
    private final String descripcion;
    private final Ranura ranura;
    private final int bonusAtaque;
    private final int bonusDefensa;

    public Equipable(String nombre, String descripcion, Ranura ranura, int bonusAtaque, int bonusDefensa) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ranura = ranura;
        this.bonusAtaque = bonusAtaque;
        this.bonusDefensa = bonusDefensa;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public Ranura getRanura() { return ranura; }
    public int getBonusAtaque() { return bonusAtaque; }
    public int getBonusDefensa() { return bonusDefensa; }
}