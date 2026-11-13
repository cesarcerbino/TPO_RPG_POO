package rpg.modelo.batalla;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rpg.modelo.accion.Accion;
import rpg.modelo.accion.AccionInvalidaException;
import rpg.modelo.accion.DefenderAccion;
import rpg.modelo.generacion.GeneradorAleatorio;
import rpg.modelo.partida.Equipo;
import rpg.modelo.unidad.Bando;
import rpg.modelo.unidad.Enemigo;
import rpg.modelo.unidad.Unidad;


public class Batalla implements Serializable {

    private final Equipo equipo;
    private final List<Enemigo> enemigos;
    private int turnoNumero;
    private final OrquestadorDeTurnos orquestador;
    private final RegistroDeCombate registro;
    private final GeneradorAleatorio rng;
    private final Set<Unidad> muertesProcesadas = new HashSet<>();

    public Batalla(Equipo equipo, List<Enemigo> enemigos, GeneradorAleatorio rng) {
        this.equipo = equipo;
        this.enemigos = new ArrayList<>(enemigos);
        this.rng = rng;
        this.turnoNumero = 1;
        this.registro = new RegistroDeCombate();

        for (Unidad u : todos()) {
            if (!u.estaVivo()) {
                muertesProcesadas.add(u);   // los que ya venían caídos no se vuelven a anunciar
            }
        }

        List<Unidad> participantes = new ArrayList<>();
        participantes.addAll(equipo.getMiembrosVivos());
        participantes.addAll(this.enemigos);
        this.orquestador = new OrquestadorDeTurnos(participantes, registro);

        registro.registrar("¡Comienza la batalla! Enfrentan a " + enemigos.size() + " enemigos.");
        orquestador.ordenarPorVelocidad();
        orquestador.iniciar();
        procesarMuertes();
    }

    public Unidad unidadEnTurno() {
        return orquestador.unidadActual();
    }

    /** La unidad en turno seguida de las que actuarían después, según el estado actual. */
    public List<Unidad> proximosTurnos(int cantidad) {
        return orquestador.proximosTurnos(cantidad);
    }

    /** ¿La unidad en turno la controla el jugador? (si no, juega la IA con jugarTurnoAutomatico). */
    public boolean esTurnoDelJugador() {
        return !finalizada() && unidadEnTurno().getBando().esControladoPorJugador();
    }

    /** La unidad en turno elige sola su acción (IA de los enemigos). */
    public void jugarTurnoAutomatico() {
        if (finalizada() || esTurnoDelJugador()) {
            return;
        }
        Unidad actor = unidadEnTurno();
        try {
            ejecutarTurno(actor.elegirAccion(this));
        } catch (AccionInvalidaException e) {
            ejecutarTurno(new DefenderAccion(actor));   // si la IA eligió mal, se defiende y no se traba la batalla
        }
    }

    /**
     * Ejecuta la acción de la unidad en turno. El Modelo valida primero: si la acción es inválida
     * lanza AccionInvalidaException y el turno NO se consume.
     */
    public void ejecutarTurno(Accion a) {
        if (finalizada()) {
            throw new AccionInvalidaException("La batalla ya terminó.");
        }
        if (a == null) {
            throw new AccionInvalidaException("No se eligió ninguna acción.");
        }
        a.validar(unidadEnTurno(), this);
        orquestador.ejecutarTurno(unidadEnTurno(), a, this);
        turnoNumero++;
        procesarMuertes();
        if (!finalizada()) {
            orquestador.avanzar();
            procesarMuertes();
        }
    }

    public boolean finalizada() {
        return equipo.estaDerrotado() || getEnemigosVivos().isEmpty();
    }

    public ResultadoBatalla getResultado() {
        boolean victoria = !equipo.estaDerrotado() && getEnemigosVivos().isEmpty();
        int xp = 0;
        if (victoria) {
            for (Enemigo e : enemigos) {
                xp += e.getExperiencia();
            }
        }
        return new ResultadoBatalla(victoria, turnoNumero - 1, xp, equipo.getMiembrosVivos());
    }


    private List<Unidad> todos() {
        List<Unidad> todos = new ArrayList<>(equipo.getMiembros());
        todos.addAll(enemigos);
        return todos;
    }


    private void procesarMuertes() {
        for (Unidad u : todos()) {
            if (!u.estaVivo() && muertesProcesadas.add(u)) {
                registro.registrar(TipoEvento.MUERTE, "¡" + u.getNombre() + " cae!");
                if (u.isInfectado()) {
                    contagiarDesde(u);
                }
            }
        }
    }

    private void contagiarDesde(Unidad muerta) {
        List<Unidad> candidatos = new ArrayList<>();
        for (Unidad v : todos()) {
            if (v != muerta && v.estaVivo() && !v.isInfectado()) {
                candidatos.add(v);
            }
        }
        if (candidatos.isEmpty()) {
            return;
        }
        Unidad elegido = candidatos.get(rng.entre(0, candidatos.size() - 1));
        elegido.infectar();
        registro.registrar(TipoEvento.EFECTO,
                "☣ " + muerta.getNombre() + " murió infectado y contagió a " + elegido.getNombre() + ".");
    }

    public List<Enemigo> getEnemigosVivos() {
        List<Enemigo> vivos = new ArrayList<>();
        for (Enemigo e : enemigos) {
            if (e.estaVivo()) {
                vivos.add(e);
            }
        }
        return vivos;
    }

    /** Unidades de un bando (vivas o no), en el orden en que la vista las numera. */
    public List<? extends Unidad> getUnidadesDe(Bando bando) {
        return bando == Bando.JUGADOR ? equipo.getMiembros() : enemigos;
    }

    /** ¿La unidad es parte de esta batalla? */
    public boolean participa(Unidad u) {
        return u != null && getUnidadesDe(u.getBando()).contains(u);
    }

    public Equipo getEquipo() { return equipo; }
    public List<Enemigo> getEnemigos() { return enemigos; }
    public int getTurnoNumero() { return turnoNumero; }
    public RegistroDeCombate getRegistro() { return registro; }
}
