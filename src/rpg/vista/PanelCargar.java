package rpg.vista;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import rpg.dto.PartidaGuardadaDTO;

/** Pantalla "Cargar partida": un bloque por partida guardada, con el nivel grande y la fecha debajo. */
public class PanelCargar extends PanelConFondo {

    private final VistaSwing vista;
    private final ListaDeBloques lista = new ListaDeBloques();

    public PanelCargar(VistaSwing vista) {
        super(165);
        this.vista = vista;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titulo = new JLabel("CARGAR PARTIDA");
        titulo.setFont(Estilo.fuente(26f));
        titulo.setForeground(Estilo.TEXTO);
        add(titulo, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(lista, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        add(scroll, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        botones.setOpaque(false);
        BotonPixel volver = new BotonPixel("Volver");
        volver.addActionListener(e -> vista.mostrarMenuPrincipal());
        botones.add(volver);
        add(botones, BorderLayout.SOUTH);
    }

    public void mostrar(List<PartidaGuardadaDTO> partidas) {
        lista.removeAll();
        for (PartidaGuardadaDTO p : partidas) {
            lista.add(new Bloque(p));
        }
        lista.revalidate();
        lista.repaint();
    }

    /** Bloque clicable de una partida guardada. */
    private class Bloque extends JComponent {

        private final PartidaGuardadaDTO p;
        private boolean encima;
        private boolean encimaBorrar;

        Bloque(PartidaGuardadaDTO p) {
            this.p = p;
            setPreferredSize(new Dimension(230, 230));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Clic para cargar. La X borra la partida.");
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (areaBorrar().contains(e.getPoint())) {
                        vista.confirmarBorrado(p);
                    } else {
                        vista.getControlador().onCargarPartida(p.nombre());
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    encimaBorrar = false;
                    repaint();
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    boolean sobre = areaBorrar().contains(e.getPoint());
                    if (sobre != encimaBorrar) {
                        encimaBorrar = sobre;
                        repaint();
                    }
                }
            });
        }

        /** Botón "X" de la esquina superior derecha. */
        private Rectangle areaBorrar() {
            return new Rectangle(getWidth() - 40, 10, 30, 26);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Estilo.calidad(g);
            int w = getWidth();
            int h = getHeight();
            Estilo.caja(g, 0, 0, w, h, Estilo.PANEL, encima ? Estilo.TOXICO : Estilo.BORDE);

            Rectangle b = areaBorrar();
            Estilo.caja(g, b.x, b.y, b.width, b.height, encimaBorrar ? Estilo.PELIGRO : Estilo.PANEL_CLARO, Estilo.BORDE);
            g.setFont(Estilo.fuente(14f));
            Estilo.textoCentrado(g, "X", b.x + b.width / 2, b.y + 19, Estilo.TEXTO);

            g.setFont(Estilo.fuente(14f));
            Estilo.textoCentrado(g, "NIVEL", w / 2, 40, Estilo.TEXTO_TENUE);
            g.setFont(Estilo.fuente(84f));
            Estilo.textoCentrado(g, String.valueOf(p.nivel()), w / 2, 125, Estilo.ORO);

            g.setFont(Estilo.fuente(17f));
            Estilo.textoCentrado(g, p.fecha(), w / 2, 185, Estilo.TEXTO);
            g.setFont(Estilo.fuente(12f));
            Estilo.textoCentrado(g, p.enBatalla() ? "EN BATALLA" : "ENTRE BATALLAS", w / 2, 210,
                    p.enBatalla() ? Estilo.PELIGRO : Estilo.TOXICO);
            g.dispose();
        }
    }
}
