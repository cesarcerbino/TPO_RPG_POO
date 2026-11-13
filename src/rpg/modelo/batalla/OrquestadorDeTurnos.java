package rpg.modelo.batalla;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rpg.modelo.accion.Accion;
import rpg.modelo.unidad.Bando;
import rpg.modelo.unidad.Unidad;


public class OrquestadorDeTurnos implements Serializable {

    private final List<Unidad> participantes;
    private final RegistroDeCombate registro;
    private final List<Unidad> ronda = new ArrayList<>();
    private int indiceActual = -1;
    private Bando ultimoBando;   // null = todavía no actuó nadie

    public OrquestadorDeTurnos(List<Unidad> participantes, RegistroDeCombate registro) {
        this.participantes = new ArrayList<>(participantes);
        this.registro = registro;
    }


    public void ordenarPorVelocidad() {
        participantes.sort(Comparator.comparingInt(Unidad::getVelocidad).reversed());
    }


    public void iniciar() {
        armarRonda();
        indiceActual = -1;
        avanzar();
    }

    public Unidad unidadActual() {
        return ronda.get(indiceActual);
    }

    public void ejecutarTurno(Unidad u, Accion a, Batalla b) {
        a.ejecutar(u, a.getObjetivo(), b);
    }


    public void avanzar() {
        for (int guarda = 0; guarda < 1000; guarda++) {
            indiceActual++;
            if (indiceActual >= ronda.size()) {
                armarRonda();
                if (ronda.isEmpty()) {
                    return;
                }
                indiceActual = 0;
            }
            Unidad u = ronda.get(indiceActual);
            if (!u.estaVivo()) {
                continue;
            }
            ultimoBando = u.getBando();

            aplicarEfectosDeTurno();
            if (!u.estaVivo()) {
                continue;
            }
            if (u.estaBloqueado()) {
                String motivo = u.consumirTurnoSalteado();
                registro.registrar(TipoEvento.EFECTO, u.getNombre() + " " + motivo);
                continue;
            }
            return;
        }
    }

    /**
     * Vista previa del orden: la unidad en turno y las que le siguen, sin modificar la batalla.
     * Saltea a los caídos y descuenta los turnos que se pierden (trampa, infección). No puede prever
     * las muertes que todavía no ocurrieron, así que es una estimación con el estado actual.
     */
    public List<Unidad> proximosTurnos(int cantidad) {
        List<Unidad> proximos = new ArrayList<>();
        if (indiceActual < 0 || indiceActual >= ronda.size()) {
            return proximos;
        }
        proximos.add(unidadActual());
        Map<Unidad, Integer> salteosPendientes = new HashMap<>();
        Bando ultimo = unidadActual().getBando();
        List<Unidad> siguientes = new ArrayList<>(ronda.subList(indiceActual + 1, ronda.size()));
        for (int guarda = 0; guarda < 50 && proximos.size() < cantidad; guarda++) {
            for (Unidad u : siguientes) {
                if (proximos.size() >= cantidad) {
                    break;
                }
                if (!u.estaVivo()) {
                    continue;
                }
                ultimo = u.getBando();
                int salteos = salteosPendientes.getOrDefault(u, u.getTurnosSalteados());
                if (salteos > 0) {
                    salteosPendientes.put(u, salteos - 1);
                    continue;
                }
                proximos.add(u);
            }
            siguientes = calcularRonda(ultimo);
            if (siguientes.isEmpty()) {
                break;
            }
        }
        return proximos;
    }

    private void armarRonda() {
        ronda.clear();
        ronda.addAll(calcularRonda(ultimoBando));
    }

    /** Arma una ronda intercalando bandos, a partir de quién actuó último. No modifica el estado. */
    private List<Unidad> calcularRonda(Bando anterior) {
        List<Unidad> equipo = new ArrayList<>();
        List<Unidad> enemigos = new ArrayList<>();
        for (Unidad u : participantes) {
            if (u.estaVivo()) {
                (u.getBando() == Bando.JUGADOR ? equipo : enemigos).add(u);
            }
        }
        List<Unidad> nueva = new ArrayList<>();
        if (equipo.isEmpty() && enemigos.isEmpty()) {
            return nueva;
        }

        boolean empiezaEquipo;
        if (anterior != null) {
            empiezaEquipo = anterior != Bando.JUGADOR;   // arranca el bando que no actuó último
        } else {
            empiezaEquipo = enemigos.isEmpty()
                    || (!equipo.isEmpty() && equipo.get(0).getVelocidad() >= enemigos.get(0).getVelocidad());
        }
        int i = 0;
        int j = 0;
        boolean turnoEquipo = empiezaEquipo;
        while (i < equipo.size() || j < enemigos.size()) {
            boolean quedaEquipo = i < equipo.size();
            boolean quedaEnemigo = j < enemigos.size();
            if (turnoEquipo && !quedaEquipo) {
                turnoEquipo = false;
            } else if (!turnoEquipo && !quedaEnemigo) {
                turnoEquipo = true;
            }
            nueva.add(turnoEquipo ? equipo.get(i++) : enemigos.get(j++));
            turnoEquipo = !turnoEquipo;
        }
        return nueva;
    }

    public void aplicarEfectosDeTurno() {
        Unidad u = unidadActual();
        List<String> log = u.alComenzarTurno();
        for (String s : log) {
            registro.registrar(TipoEvento.EFECTO,s);
        }
    }
}
