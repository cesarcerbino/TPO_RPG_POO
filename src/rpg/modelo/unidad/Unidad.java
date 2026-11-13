package rpg.modelo.unidad;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rpg.modelo.accion.Accion;
import rpg.modelo.batalla.Batalla;
import rpg.modelo.equipamiento.Equipable;
import rpg.modelo.habilidad.Habilidad;
import rpg.modelo.objeto.Objeto;

public abstract class Unidad implements Serializable {

    public static final int TURNOS_BLOQUEO_INFECCION = 2;

    protected String nombre;
    protected int vida;
    protected int vidaMaxima;
    protected int velocidad;
    protected int ataque;
    protected int defensa;
    protected List<Habilidad> habilidades = new ArrayList<>();


    private boolean defendiendo;
    private boolean infectado;
    private int turnosSalteados;
    private int turnosBloqueoInfeccion;

    private static final CalculadorDeDano CALCULADOR = CalculadorDeDano.getInstancia();

    protected Unidad(String nombre, int vida, int ataque, int defensa, int velocidad) {
        this.nombre = nombre;
        this.vida = vida;
        this.vidaMaxima = vida;
        this.ataque = ataque;
        this.defensa = defensa;
        this.velocidad = velocidad;
    }


    //  Abstracciones sin instanceof//
    public abstract Bando getBando();
    public abstract TipoUnidad getTipo();
    public abstract Accion elegirAccion(Batalla b);
    public boolean esAliadoDe(Unidad otra){
        return otra != null && getBando() == otra.getBando();
    }


    public abstract void atacar(Unidad obj);


    protected void golpear(Unidad obj) {
        obj.recibirDano(CALCULADOR.calcular(this, obj));
    }

    public void recibirDano(int cantidad) {
        vida = Math.max(0, vida - Math.max(0, cantidad));
    }


    public int curar(int cantidad) {
        if (!estaVivo()) {
            return 0;
        }
        int antes = vida;
        vida = Math.min(vidaMaxima, vida + Math.max(0, cantidad));
        return vida - antes;
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void usarHabilidad(Habilidad h, Unidad o, Batalla b) {
        h.ejecutar(this, o, b);
    }

    public void usarObjeto(Objeto o, Unidad obj, Batalla b){
        o.usar(this, obj, b);
    }


    // Costo de las habildiades//
    public boolean puedePagar(int costoMana){
        return true;
    }
    public void pagar(int costoMana){

    }
    public void alDefender(){
        setDefendiendo(true);
    }

    // DAtos progreso. Personaje los redefine//
    public int getMana(){ return 0; }
    public int getManaMaximo(){ return 0;}
    public int getNivel(){ return 0; }
    public int getExperiencia(){ return 0;}
    public int experienciaParaSubir(){ return 0; }

    /** Armas y armaduras puestas. Los enemigos no usan equipamiento; Personaje lo redefine. */
    public List<Equipable> getEquipados(){ return new ArrayList<>(); }


// Infección y bloqueo//
    public boolean isInfectado() { return infectado; }

    public void infectar() {
        if (!infectado) {
            infectado = true;
            turnosBloqueoInfeccion = TURNOS_BLOQUEO_INFECCION;
        }
    }
    public void curarInfeccion() {
        this.infectado = false;
        this.turnosBloqueoInfeccion = 0;
    }


    public int aplicarInfeccion() {
        if (!infectado || !estaVivo()) {
            return 0;
        }
        int dano = Math.max(2, vidaMaxima * 5 / 100);
        recibirDano(dano);
        return dano;
    }

    public List<String> alComenzarTurno(){
        this.setDefendiendo(false);
        int danio = this.aplicarInfeccion();
        List<String> log = new ArrayList<>();
        if(danio > 0 ){
            log.add(this.getNombre() + " sufre " + danio + " de daño por la infección.");
        }
        return log;
    }

    public int getTurnosSalteados() { return turnosSalteados + turnosBloqueoInfeccion; }

    public boolean estaBloqueado() { return getTurnosSalteados() > 0; }
    public void saltearTurnos(int cantidad) {
        this.turnosSalteados = Math.max(this.turnosSalteados, cantidad);
    }

    public String consumirTurnoSalteado() {
        if (turnosBloqueoInfeccion > 0){
            turnosBloqueoInfeccion --;
            return "está infectado y pierde el turno.";
        }
        if (turnosSalteados > 0){
            turnosSalteados--;
            return "está atrapado y pierde el turno.";
        }
        return "";
    }



    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getVida() { return vida; }
    public int getVidaMaxima() { return vidaMaxima; }
    public int getAtaque() { return ataque; }
    public int getDefensa() { return defensa; }
    public List<Habilidad> getHabilidades() { return habilidades; }
    public boolean isDefendiendo() { return defendiendo; }
    public void setDefendiendo(boolean defendiendo) { this.defendiendo = defendiendo; }
}
