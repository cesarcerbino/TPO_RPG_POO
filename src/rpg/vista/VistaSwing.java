package rpg.vista;

import java.awt.CardLayout;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import rpg.controlador.ControladorJuego;
import rpg.controlador.IVista;
import rpg.dto.BatallaDTO;
import rpg.dto.EquipamientoDTO;
import rpg.dto.PartidaDTO;
import rpg.dto.PartidaGuardadaDTO;
import rpg.dto.ResultadoBatallaDTO;
import rpg.dto.UnidadDTO;

/**
 * Vista Swing. Reemplaza a VistaConsola del diagrama. No importa nada de rpg.modelo:
 * solo habla con el controlador y recibe DTO.
 */
public class VistaSwing extends JFrame implements IVista {

    private static final String MENU = "menu";
    private static final String SELECCION = "seleccion";
    private static final String CARGAR = "cargar";
    private static final String BATALLA = "batalla";
    private static final String RESULTADO = "resultado";
    private static final String EQUIPAMIENTO = "equipamiento";

    private static final int PAUSA_TURNO_ENEMIGO = 850;
    private static final int PAUSA_FIN_DE_BATALLA = 1300;

    private final CardLayout cards = new CardLayout();
    private final JPanel contenedor = new JPanel(cards);

    private final PanelMenu panelMenu;
    private final PanelSeleccion panelSeleccion;
    private final PanelCargar panelCargar;
    private final PanelBatalla panelBatalla;
    private final PanelResultado panelResultado;
    private final PanelEquipamiento panelEquipamiento;

    private ControladorJuego controlador;

    // El controlador resuelve todos los turnos enemigos de una vez. Para que se vean de a uno, todo lo
    // que pide se encola y se muestra en orden; los turnos enemigos dejan una pausa antes del paso siguiente.
    private final Deque<Runnable> pasos = new ArrayDeque<>();
    private Timer pausa;
    private boolean procesando;

    public VistaSwing() {
        super("RPG por turnos - Sobrevivientes vs. zombies");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1180, 800);
        setMinimumSize(new java.awt.Dimension(960, 680));
        setLocationRelativeTo(null);

        panelMenu = new PanelMenu(this);
        panelSeleccion = new PanelSeleccion(this);
        panelCargar = new PanelCargar(this);
        panelBatalla = new PanelBatalla(this);
        panelResultado = new PanelResultado(this);
        panelEquipamiento = new PanelEquipamiento(this);

        contenedor.add(panelMenu, MENU);
        contenedor.add(panelSeleccion, SELECCION);
        contenedor.add(panelCargar, CARGAR);
        contenedor.add(panelBatalla, BATALLA);
        contenedor.add(panelResultado, RESULTADO);
        contenedor.add(panelEquipamiento, EQUIPAMIENTO);
        setContentPane(contenedor);
    }

    public void setControlador(ControladorJuego controlador) {
        this.controlador = controlador;
    }

    public ControladorJuego getControlador() {
        return controlador;
    }

    // ---------------------------------------------------------------- cola de pasos

    private void encolar(Runnable paso) {
        pasos.add(paso);
        procesarPasos();
    }

    private void procesarPasos() {
        if (procesando) {
            return;
        }
        procesando = true;
        try {
            while (pausa == null && !pasos.isEmpty()) {
                pasos.poll().run();
            }
        } finally {
            procesando = false;
        }
    }

    private void pausar(int milisegundos) {
        pausa = new Timer(milisegundos, e -> {
            pausa = null;
            procesarPasos();
        });
        pausa.setRepeats(false);
        pausa.start();
    }

    /** Muestra lo que quedaba pendiente sin pausas (por ejemplo, al salir de la batalla a mitad de un turno enemigo). */
    private void adelantarPasos() {
        do {
            if (pausa != null) {
                pausa.stop();
                pausa = null;
            }
            Runnable paso = pasos.poll();
            if (paso != null) {
                paso.run();
            }
        } while (!pasos.isEmpty() || pausa != null);
    }

    // ---------------------------------------------------------------- IVista

    @Override
    public void mostrarMenuPrincipal() {
        adelantarPasos();
        cards.show(contenedor, MENU);
    }

    @Override
    public void mostrarSeleccionPersonajes(List<UnidadDTO> disponibles, int minimo, int maximo) {
        encolar(() -> {
            panelSeleccion.mostrar(disponibles, minimo, maximo);
            cards.show(contenedor, SELECCION);
        });
    }

    @Override
    public void mostrarBatalla(BatallaDTO batalla) {
        encolar(() -> {
            panelBatalla.mostrar(batalla);
            cards.show(contenedor, BATALLA);
        });
    }

    @Override
    public void mostrarTurnoEnemigo(BatallaDTO batalla) {
        encolar(() -> {
            panelBatalla.mostrarTurnoEnemigo(batalla);
            cards.show(contenedor, BATALLA);
            pausar(PAUSA_TURNO_ENEMIGO);
        });
    }

    @Override
    public void mostrarFinDeBatalla(BatallaDTO batalla) {
        encolar(() -> {
            panelBatalla.mostrarFinDeBatalla(batalla);
            cards.show(contenedor, BATALLA);
            pausar(PAUSA_FIN_DE_BATALLA);
        });
    }

    @Override
    public void mostrarResultado(ResultadoBatallaDTO resultado, PartidaDTO partida) {
        encolar(() -> {
            panelResultado.mostrar(resultado, partida);
            cards.show(contenedor, RESULTADO);
        });
    }

    @Override
    public void mostrarEquipamiento(EquipamientoDTO equipamiento) {
        encolar(() -> {
            panelEquipamiento.mostrar(equipamiento);
            cards.show(contenedor, EQUIPAMIENTO);
        });
    }

    /** Desde el equipamiento se vuelve al resultado tal como quedó: no hace falta pedirle nada al controlador. */
    public void volverAlResultado() {
        cards.show(contenedor, RESULTADO);
    }

    @Override
    public void mostrarEvento(String texto) {
        encolar(() -> panelBatalla.agregarEvento(texto));
    }

    @Override
    public void limpiarRegistro() {
        encolar(panelBatalla::limpiarRegistro);
    }

    @Override
    public void mostrarMensaje(String texto) {
        encolar(() -> JOptionPane.showMessageDialog(this, texto));
    }

    @Override
    public void mostrarPartidasGuardadas(List<PartidaGuardadaDTO> partidas) {
        encolar(() -> {
            panelCargar.mostrar(partidas);
            cards.show(contenedor, CARGAR);
        });
    }

    /** Pide confirmación antes de borrar una partida guardada (la opción por defecto es cancelar). */
    public void confirmarBorrado(PartidaGuardadaDTO partida) {
        Object[] opciones = {"Borrar", "Cancelar"};
        int r = JOptionPane.showOptionDialog(this,
                "¿Seguro que querés borrar la partida del nivel " + partida.nivel() + " (" + partida.fecha() + ")?\n"
                        + "Esta acción no se puede deshacer.",
                "Borrar partida", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, opciones, opciones[1]);
        if (r == 0) {
            controlador.onBorrarPartida(partida.nombre());
        }
    }

    /** Menú de la batalla en curso: guardar y salir, salir sin guardar, o seguir jugando. */
    public void confirmarVolverAlMenu() {
        Object[] opciones = {"Guardar y volver al menú", "Volver sin guardar", "Cancelar"};
        int r = JOptionPane.showOptionDialog(this, "¿Qué querés hacer con la partida en curso?", "Volver al menú",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (r == 0) {
            controlador.onGuardarYVolverAlMenu();
        } else if (r == 1) {
            controlador.onVolverAlMenu();
        }
    }
}
