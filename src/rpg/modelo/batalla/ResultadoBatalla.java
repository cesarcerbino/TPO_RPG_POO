package rpg.modelo.batalla;

import java.util.ArrayList;
import java.util.List;

import rpg.modelo.objeto.Objeto;
import rpg.modelo.unidad.Personaje;
import rpg.modelo.equipamiento.Equipable;
public class ResultadoBatalla {

    private final boolean victoria;
    private final int turnosUsados;
    private final int experienciaGanada;
    private final List<Personaje> sobrevivientes;
    private final List<String> recompensas = new ArrayList<>();
    private final List<Objeto> botin = new ArrayList<>();
    private final List<Equipable> equipables = new ArrayList<>();

    public ResultadoBatalla(boolean victoria, int turnosUsados, int experienciaGanada, List<Personaje> sobrevivientes) {
        this.victoria = victoria;
        this.turnosUsados = turnosUsados;
        this.experienciaGanada = experienciaGanada;
        this.sobrevivientes = new ArrayList<>(sobrevivientes);
    }


    public void agregarRecompensa(String texto) {
        recompensas.add(texto);
    }

    public void agregarBotin(Objeto objeto) {
        botin.add(objeto);
    }

    public void agregarEquipable(Equipable e) {
        equipables.add(e);
    }

    public boolean fueVictoria() { return victoria; }
    public int getTurnosUsados() { return turnosUsados; }
    public int getExperiencia() { return experienciaGanada; }
    public List<Personaje> getSobrevivientes() { return sobrevivientes; }
    public List<String> getRecompensas() { return recompensas; }
    public List<Objeto> getBotin() { return botin; }
    public List<Equipable> getEquipables() { return equipables; }

}
