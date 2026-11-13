package rpg.modelo.unidad;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import rpg.modelo.accion.Accion;
import rpg.modelo.accion.DefenderAccion;
import rpg.modelo.batalla.Batalla;
import rpg.modelo.equipamiento.Equipable;
import rpg.modelo.equipamiento.Ranura;
import rpg.modelo.habilidad.Habilidad;


public abstract class Personaje extends Unidad {

    public static final int MANA_POR_TURNO = 3;
    public static final int MANA_AL_DEFENDER = 5;

    protected int mana;
    protected int manaMaximo;
    protected int nivel = 1;
    protected int experiencia;
    private final Map<Ranura, Equipable> equipables = new EnumMap<>(Ranura.class);

    protected int crecimientoVida = 10;
    protected int crecimientoMana = 5;
    protected int crecimientoAtaque = 2;

    protected Personaje(String nombre, int vida, int mana, int ataque, int defensa, int velocidad) {
        super(nombre, vida, ataque, defensa, velocidad);
        this.mana = mana;
        this.manaMaximo = mana;
    }

    @Override
    public Bando getBando(){
        return Bando.JUGADOR;
    }
    @Override
    public Accion elegirAccion(Batalla b){
        return new DefenderAccion(this);
    }
    @Override
    public boolean puedePagar(int costoMana){
        return tieneMana(costoMana);
    }
    @Override
    public void pagar(int costoMana){
        gastarMana(costoMana);
    }
    @Override
    public void alDefender(){
        super.alDefender();
        recuperarMana(MANA_AL_DEFENDER);
    }

    public boolean tieneMana(int costo) {
        return mana >= costo;
    }

    public void gastarMana(int costo) {
        mana = Math.max(0, mana - costo);
    }

    public void recuperarMana(int cantidad) {
        mana = Math.min(manaMaximo, mana + cantidad);
    }

    public void restaurarMana() {
        mana = manaMaximo;
    }

    @Override
    public int experienciaParaSubir() {
        return 40 * nivel;
    }


    public List<String> ganarExperiencia(int xp) {
        List<String> mensajes = new ArrayList<>();
        mensajes.add(nombre + " gana " + xp + " de experiencia.");
        experiencia += xp;
        while (experiencia >= experienciaParaSubir()) {
            experiencia -= experienciaParaSubir();
            subirNivel();
            mensajes.add("¡" + nombre + " sube al nivel " + nivel + "! (vida " + vidaMaxima + ", maná " + manaMaximo
                    + ", ataque " + getAtaque() + ", defensa " + getDefensa() + ", velocidad " + velocidad + ")");
        }
        return mensajes;
    }

    protected void subirNivel() {
        nivel++;
        vidaMaxima += crecimientoVida;
        if (estaVivo()) {
            vida = Math.min(vidaMaxima, vida + crecimientoVida);
        }
        manaMaximo += crecimientoMana;
        mana = Math.min(manaMaximo, mana + crecimientoMana);
        ataque += crecimientoAtaque;
        defensa += 1;
        if (nivel % 2 == 0) {
            velocidad += 1;
        }
        for (Habilidad h : habilidades) {
            h.setNivel(nivel);
        }
    }

    @Override
    public int getAtaque() {
        int total = super.getAtaque();
        for (Equipable e : equipables.values()) {
            total += e.getBonusAtaque();
        }
        return total;
    }

    @Override
    public int getDefensa() {
        int total = super.getDefensa();
        for (Equipable e : equipables.values()) {
            total += e.getBonusDefensa();
        }
        return Math.max(0, total);
    }

    @Override
    public List<String> alComenzarTurno(){
        List<String> log = super.alComenzarTurno();
        this.recuperarMana(MANA_POR_TURNO);
        return log;
    }

    public Equipable equipar(Equipable e) {
        return equipables.put(e.getRanura(), e);
    }

    public Equipable getEquipado(Ranura r) {
        return equipables.get(r);
    }

    /** Lo que tiene puesto, en el orden de las ranuras (arma, armadura). */
    @Override
    public List<Equipable> getEquipados() {
        return new ArrayList<>(equipables.values());
    }

    @Override
    public int getMana() { return mana; }
    @Override
    public int getManaMaximo() { return manaMaximo; }
    @Override
    public int getNivel() { return nivel; }
    @Override
    public int getExperiencia() { return experiencia; }
}
