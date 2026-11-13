package rpg.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import rpg.dto.BatallaDTO;
import rpg.dto.HabilidadDTO;
import rpg.dto.ObjetoDTO;
import rpg.dto.UnidadDTO;

/** Pantalla de combate: encabezado, escena animada, caja de comandos y registro de combate. */
public class PanelBatalla extends JPanel {

    private static final String ACCIONES = "acciones";
    private static final String LISTA = "lista";
    private static final String OBJETIVO = "objetivo";
    private static final String ESPERA = "espera";

    private final VistaSwing vista;
    private final EscenaBatalla escena = new EscenaBatalla();
    private final BarraDeTurnos barraDeTurnos = new BarraDeTurnos();
    private final JLabel lblEstado = new JLabel();
    private final JTextArea log = new JTextArea();

    private final CardLayout cartas = new CardLayout();
    private final JPanel comandos = new JPanel(cartas);
    private final JPanel panelLista = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JLabel lblAyuda = new JLabel();
    private final JLabel lblEspera = new JLabel();
    private final FichaDeStats ficha = new FichaDeStats();
    private final BotonPixel btnHabilidad = new BotonPixel("Habilidad");
    private final BotonPixel btnObjeto = new BotonPixel("Objeto");

    private BatallaDTO actual;

    public PanelBatalla(VistaSwing vista) {
        this.vista = vista;
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);

        lblEstado.setFont(Estilo.fuente(15f));
        lblEstado.setForeground(Estilo.TEXTO);
        JPanel encabezado = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Estilo.PANEL);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Estilo.BORDE);
                g.fillRect(0, getHeight() - 3, getWidth(), 3);
            }
        };
        encabezado.setBorder(BorderFactory.createEmptyBorder(8, 16, 10, 16));
        encabezado.add(lblEstado, BorderLayout.CENTER);
        BotonPixel btnMenu = new BotonPixel("Menú");
        btnMenu.setPreferredSize(new Dimension(110, 36));
        btnMenu.addActionListener(e -> vista.confirmarVolverAlMenu());
        encabezado.add(btnMenu, BorderLayout.EAST);
        JPanel superior = new JPanel(new BorderLayout());
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(barraDeTurnos, BorderLayout.SOUTH);
        add(superior, BorderLayout.NORTH);

        add(escena, BorderLayout.CENTER);
        add(construirInferior(), BorderLayout.SOUTH);
    }

    private JPanel construirInferior() {
        JPanel inferior = new JPanel(new TresColumnas(10)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Estilo.PANEL);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Estilo.BORDE);
                g.fillRect(0, 0, getWidth(), 3);
            }
        };
        inferior.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        inferior.setPreferredSize(new Dimension(0, 176));

        comandos.setOpaque(false);
        comandos.add(construirAcciones(), ACCIONES);
        panelLista.setOpaque(false);
        comandos.add(panelLista, LISTA);
        comandos.add(construirAyuda(), OBJETIVO);
        comandos.add(construirEspera(), ESPERA);
        inferior.add(comandos);
        inferior.add(ficha);

        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setFont(Estilo.fuente(12f));
        log.setForeground(new Color(190, 235, 170));
        log.setBackground(new Color(12, 12, 16));
        log.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(log, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE, 3));
        inferior.add(scroll);
        return inferior;
    }

    /**
     * Comandos | ficha | registro: la ficha (componente del medio) queda centrada en el ancho de la ventana
     * con su ancho preferido, y los otros dos se reparten el espacio que sobra a cada lado.
     */
    private static final class TresColumnas implements java.awt.LayoutManager {

        private final int separacion;

        TresColumnas(int separacion) {
            this.separacion = separacion;
        }

        @Override
        public void layoutContainer(java.awt.Container padre) {
            java.awt.Insets in = padre.getInsets();
            int w = padre.getWidth() - in.left - in.right;
            int h = padre.getHeight() - in.top - in.bottom;
            java.awt.Component izquierda = padre.getComponent(0);
            java.awt.Component medio = padre.getComponent(1);
            java.awt.Component derecha = padre.getComponent(2);
            int anchoMedio = Math.min(medio.getPreferredSize().width, w);
            int lateral = Math.max(0, (w - anchoMedio) / 2 - separacion);
            izquierda.setBounds(in.left, in.top, lateral, h);
            medio.setBounds(in.left + (w - anchoMedio) / 2, in.top, anchoMedio, h);
            derecha.setBounds(in.left + w - lateral, in.top, lateral, h);
        }

        @Override
        public Dimension preferredLayoutSize(java.awt.Container padre) {
            java.awt.Insets in = padre.getInsets();
            return new Dimension(in.left + in.right, in.top + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(java.awt.Container padre) {
            return preferredLayoutSize(padre);
        }

        @Override
        public void addLayoutComponent(String nombre, java.awt.Component c) {
        }

        @Override
        public void removeLayoutComponent(java.awt.Component c) {
        }
    }

    private JPanel construirAcciones() {
        JPanel p = new JPanel(new java.awt.GridLayout(2, 2, 10, 10));
        p.setOpaque(false);
        BotonPixel atacar = new BotonPixel("Atacar");
        BotonPixel defender = new BotonPixel("Defender");
        atacar.addActionListener(e -> accionAtacar());
        btnHabilidad.addActionListener(e -> accionHabilidad());
        btnObjeto.addActionListener(e -> accionObjeto());
        defender.addActionListener(e -> vista.getControlador().onDefender());
        p.add(atacar);
        p.add(btnHabilidad);
        p.add(btnObjeto);
        p.add(defender);
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(p, BorderLayout.CENTER);
        return envoltorio;
    }

    private JPanel construirAyuda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setOpaque(false);
        lblAyuda.setFont(Estilo.fuente(15f));
        lblAyuda.setForeground(Estilo.ORO);
        BotonPixel cancelar = new BotonPixel("Cancelar");
        cancelar.addActionListener(e -> volverAAcciones());
        p.add(lblAyuda);
        p.add(cancelar);
        return p;
    }

    /** Reemplaza a los botones mientras actúan los enemigos: el jugador no puede elegir nada. */
    private JPanel construirEspera() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 18));
        p.setOpaque(false);
        lblEspera.setFont(Estilo.fuente(15f));
        p.add(lblEspera);
        return p;
    }

    // ---------------------------------------------------------------- registro (Observador)

    public void limpiarRegistro() {
        log.setText("");
        escena.reiniciar();
    }

    public void agregarEvento(String texto) {
        log.append("> " + texto + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    // ---------------------------------------------------------------- estado de la batalla

    public void mostrar(BatallaDTO b) {
        this.actual = b;
        UnidadDTO turno = b.unidadEnTurno();
        mostrarEncabezado(b, "ACTÚA: " + turno.nombre().toUpperCase());
        escena.mostrar(b);
        ficha.mostrar(turno);
        barraDeTurnos.mostrar(b.proximosTurnos());
        btnHabilidad.setEnabled(!turno.habilidades().isEmpty());
        btnObjeto.setEnabled(!b.objetos().isEmpty());
        cartas.show(comandos, ACCIONES);
    }

    /** Turno de un enemigo: se marca quién va a atacar y se ocultan los botones hasta que vuelva a tocarle al equipo. */
    public void mostrarTurnoEnemigo(BatallaDTO b) {
        this.actual = b;
        UnidadDTO turno = b.unidadEnTurno();
        mostrarEncabezado(b, "ACTÚA: " + turno.nombre().toUpperCase());
        escena.mostrar(b, true, "TURNO DE " + turno.nombre().toUpperCase(), Estilo.PELIGRO);
        barraDeTurnos.mostrar(b.proximosTurnos());
        ficha.mostrar(turno);
        lblEspera.setForeground(Estilo.PELIGRO);
        lblEspera.setText("Turno enemigo: " + turno.nombre() + "...");
        cartas.show(comandos, ESPERA);
    }

    /** Último estado de la batalla: se ve el golpe final antes de pasar a la pantalla de resultado. */
    public void mostrarFinDeBatalla(BatallaDTO b) {
        this.actual = b;
        boolean victoria = b.enemigos().stream().noneMatch(UnidadDTO::vivo);
        mostrarEncabezado(b, "FIN DE LA BATALLA");
        escena.mostrar(b, false, victoria ? "¡VICTORIA!" : "EL EQUIPO CAYÓ", victoria ? Estilo.TOXICO : Estilo.PELIGRO);
        barraDeTurnos.mostrar(List.of());
        lblEspera.setForeground(victoria ? Estilo.TOXICO : Estilo.PELIGRO);
        lblEspera.setText(victoria ? "No queda ningún zombie en pie." : "Todo el equipo cayó.");
        cartas.show(comandos, ESPERA);
    }

    private void mostrarEncabezado(BatallaDTO b, String detalle) {
        lblEstado.setText("NIVEL " + b.nivel() + "    TURNO " + b.turno() + "    " + detalle);
    }

    private void volverAAcciones() {
        escena.cancelarEleccion();
        cartas.show(comandos, ACCIONES);
    }

    // ---------------------------------------------------------------- acciones

    private void pedirObjetivo(String ayuda, boolean aliados, java.util.function.IntConsumer alElegir) {
        lblAyuda.setText(ayuda);
        cartas.show(comandos, OBJETIVO);
        escena.pedirObjetivo(aliados, alElegir);
    }

    private void accionAtacar() {
        pedirObjetivo("¿A quién atacás? Hacé clic en un enemigo.", false,
                idx -> vista.getControlador().onAtacar(idx));
    }

    private void accionHabilidad() {
        UnidadDTO turno = actual.unidadEnTurno();
        panelLista.removeAll();
        for (HabilidadDTO h : turno.habilidades()) {
            BotonPixel b = new BotonPixel(h.nombre() + " (" + h.costoMana() + " maná)");
            b.setPreferredSize(new Dimension(250, 46));
            b.setToolTipText(h.descripcion());
            b.setEnabled(turno.mana() >= h.costoMana());
            b.addActionListener(e -> usarHabilidad(h));
            panelLista.add(b);
        }
        mostrarLista("Maná: " + turno.mana());
    }

    private void usarHabilidad(HabilidadDTO h) {
        if (h.requiereObjetivo()) {
            pedirObjetivo(h.nombre() + ": " + (h.paraAliados() ? "elegí un aliado." : "elegí un enemigo."), h.paraAliados(),
                    idx -> vista.getControlador().onUsarHabilidad(h.indice(), idx));
        } else {
            vista.getControlador().onUsarHabilidad(h.indice(), -1);
        }
    }

    private void accionObjeto() {
        panelLista.removeAll();
        for (ObjetoDTO o : actual.objetos()) {
            BotonPixel b = new BotonPixel(o.nombre() + " x" + o.usosRestantes());
            b.setPreferredSize(new Dimension(190, 46));
            b.setToolTipText(o.descripcion() + " (consume el turno)");
            b.addActionListener(e -> usarObjeto(o));
            panelLista.add(b);
        }
        mostrarLista("Los objetos consumen el turno");
    }

    private void usarObjeto(ObjetoDTO o) {
        if (o.requiereObjetivo()) {
            pedirObjetivo(o.nombre() + ": " + (o.paraAliados() ? "elegí un aliado." : "elegí un enemigo."), o.paraAliados(),
                    idx -> vista.getControlador().onUsarObjeto(o.indice(), idx));
        } else {
            vista.getControlador().onUsarObjeto(o.indice(), -1);
        }
    }

    private void mostrarLista(String nota) {
        JLabel l = new JLabel(nota);
        l.setFont(Estilo.fuente(12f));
        l.setForeground(Estilo.TEXTO_TENUE);
        BotonPixel volver = new BotonPixel("Volver");
        volver.setPreferredSize(new Dimension(110, 46));
        volver.addActionListener(e -> volverAAcciones());
        panelLista.add(volver);
        panelLista.add(l);
        panelLista.revalidate();
        panelLista.repaint();
        cartas.show(comandos, LISTA);
    }
}
