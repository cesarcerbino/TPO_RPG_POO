package rpg.controlador;

import java.util.ArrayList;
import java.util.List;

import rpg.modelo.accion.Accion;
import rpg.modelo.accion.AccionInvalidaException;
import rpg.modelo.accion.AtacarAccion;
import rpg.modelo.accion.DefenderAccion;
import rpg.modelo.accion.UsarHabilidadAccion;
import rpg.modelo.accion.UsarObjetoAccion;
import rpg.modelo.batalla.Batalla;
import rpg.modelo.batalla.ResultadoBatalla;
import rpg.modelo.habilidad.Habilidad;
import rpg.modelo.habilidad.Objetivo;
import rpg.modelo.objeto.Objeto;
import rpg.modelo.partida.Partida;
import rpg.modelo.unidad.Bando;
import rpg.modelo.unidad.Personaje;
import rpg.modelo.unidad.Unidad;

/**
 * Responsabilidad única: el flujo de UNA batalla. Traduce lo que elige el jugador (índices de la vista)
 * a acciones del modelo, deja jugar a la IA y avisa a la vista. Las reglas las valida el Modelo.
 */
public class ControladorBatalla {

    private final IVista vista;
    private Partida partida;
    private Batalla batalla;

    public ControladorBatalla(IVista vista) {
        this.vista = vista;
    }

    /** Empieza (o retoma) una batalla: conecta el registro con la vista y avanza hasta el turno del jugador. */
    public void jugar(Partida partida, Batalla b) {
        this.partida = partida;
        this.batalla = b;
        // Observador: la vista recibe cada evento del registro apenas ocurre.
        vista.limpiarRegistro();
        for (String linea : batalla.getRegistro().getHistorial()) {
            vista.mostrarEvento(linea);
        }
        batalla.getRegistro().agregarObservador(evento -> vista.mostrarEvento(evento.getTexto()));
        avanzarBatalla();
    }

    /** Se abandona la batalla (volver al menú). */
    public void soltar() {
        partida = null;
        batalla = null;
    }

    // ------------------------------------------------------------ acciones del jugador

    public void onAtacar(int indiceEnemigo) {
        if (esTurnoDelJugador()) {
            ejecutarAccionDelJugador(new AtacarAccion(unidad(Bando.ENEMIGO, indiceEnemigo)));
        }
    }

    public void onUsarHabilidad(int indiceHabilidad, int indiceObjetivo) {
        if (!esTurnoDelJugador()) {
            return;
        }
        List<Habilidad> habs = batalla.unidadEnTurno().getHabilidades();
        if (indiceHabilidad < 0 || indiceHabilidad >= habs.size()) {
            vista.mostrarMensaje("Habilidad inválida.");
            return;
        }
        Habilidad h = habs.get(indiceHabilidad);
        ejecutarAccionDelJugador(new UsarHabilidadAccion(h, objetivo(h.getTipoObjetivo(), indiceObjetivo)));
    }

    public void onUsarObjeto(int indiceObjeto, int indiceObjetivo) {
        if (!esTurnoDelJugador()) {
            return;
        }
        List<Objeto> objetos = batalla.getEquipo().getInventario().getDisponibles();
        if (indiceObjeto < 0 || indiceObjeto >= objetos.size()) {
            vista.mostrarMensaje("Objeto inválido.");
            return;
        }
        Objeto o = objetos.get(indiceObjeto);
        ejecutarAccionDelJugador(new UsarObjetoAccion(o, objetivo(o.getTipoObjetivo(), indiceObjetivo)));
    }

    public void onDefender() {
        if (esTurnoDelJugador()) {
            ejecutarAccionDelJugador(new DefenderAccion(batalla.unidadEnTurno()));
        }
    }

    // ------------------------------------------------------------ flujo

    private void ejecutarAccionDelJugador(Accion accion) {
        try {
            batalla.ejecutarTurno(accion);
        } catch (AccionInvalidaException e) {
            vista.mostrarMensaje(e.getMessage());   // el Modelo la rechazó: el turno no se consumió
            return;
        }
        avanzarBatalla();
    }

    private void avanzarBatalla() {
        while (!batalla.finalizada() && !batalla.esTurnoDelJugador()) {
            // La vista se entera de cada turno de la IA por separado y decide cómo mostrarlo (pausas, animación).
            vista.mostrarTurnoEnemigo(ConversorDTO.batalla(partida, batalla));
            batalla.jugarTurnoAutomatico();
        }
        if (batalla.finalizada()) {
            terminarBatalla();
        } else {
            vista.mostrarBatalla(ConversorDTO.batalla(partida, batalla));
        }
    }

    private void terminarBatalla() {
        vista.mostrarFinDeBatalla(ConversorDTO.batalla(partida, batalla));
        ResultadoBatalla resultado = batalla.getResultado();
        List<Personaje> miembros = batalla.getEquipo().getMiembros();
        List<Integer> nivelesAntes = new ArrayList<>();
        for (Personaje p : miembros) {
            nivelesAntes.add(p.getNivel());
        }
        partida.otorgarRecompensas(resultado);
        partida.getEstado().actualizar(partida);
        vista.mostrarResultado(ConversorDTO.resultado(resultado, miembros, nivelesAntes), ConversorDTO.partida(partida));
    }

    // ------------------------------------------------------------ índices de la vista -> unidades del modelo

    private boolean esTurnoDelJugador() {
        return batalla != null && batalla.esTurnoDelJugador();
    }

    /** Traduce el índice que eligió la vista según el tipo de objetivo. Si no hace falta objetivo, null. */
    private Unidad objetivo(Objetivo tipo, int indice) {
        if (!tipo.requiereSeleccion()) {
            return null;
        }
        Bando propio = batalla.unidadEnTurno().getBando();
        return unidad(tipo.esDeAliados() ? propio : propio.rival(), indice);
    }

    /** Unidad de un bando por índice, o null si el índice no existe (el Modelo después lo rechaza). */
    private Unidad unidad(Bando bando, int indice) {
        List<? extends Unidad> lista = batalla.getUnidadesDe(bando);
        return indice >= 0 && indice < lista.size() ? lista.get(indice) : null;
    }
}
