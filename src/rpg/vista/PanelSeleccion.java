package rpg.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import rpg.dto.HabilidadDTO;
import rpg.dto.UnidadDTO;

public class PanelSeleccion extends PanelConFondo {

    private final VistaSwing vista;
    private final JLabel lblTitulo = new JLabel();
    private final JLabel lblContador = new JLabel();
    private final ListaDeBloques lista = new ListaDeBloques();
    private final List<Carta> cartas = new ArrayList<>();
    private int minimo;
    private int maximo;

    public PanelSeleccion(VistaSwing vista) {
        super(165);
        this.vista = vista;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        lblTitulo.setFont(Estilo.fuente(26f));
        lblTitulo.setForeground(Estilo.TEXTO);
        lblContador.setFont(Estilo.fuente(16f));
        lblContador.setForeground(Estilo.ORO);
        norte.add(lblTitulo, BorderLayout.WEST);
        norte.add(lblContador, BorderLayout.EAST);
        add(norte, BorderLayout.NORTH);

        lista.setOpaque(false);
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
        BotonPixel comenzar = new BotonPixel("Comenzar");
        comenzar.addActionListener(e -> confirmar());
        botones.add(volver);
        botones.add(comenzar);
        add(botones, BorderLayout.SOUTH);
    }

    public void mostrar(List<UnidadDTO> disponibles, int minimo, int maximo) {
        this.minimo = minimo;
        this.maximo = maximo;
        lblTitulo.setText("ELEGÍ TU EQUIPO (" + minimo + " a " + maximo + ")");
        lista.removeAll();
        cartas.clear();
        for (UnidadDTO u : disponibles) {
            Carta c = new Carta(u);
            cartas.add(c);
            lista.add(c);
        }
        actualizarContador();
        lista.revalidate();
        lista.repaint();
    }

    private int elegidos() {
        int n = 0;
        for (Carta c : cartas) {
            if (c.elegida) {
                n++;
            }
        }
        return n;
    }

    private void actualizarContador() {
        lblContador.setText("Elegidos: " + elegidos() + " / " + maximo);
    }

    private void confirmar() {
        List<Integer> elegidos = new ArrayList<>();
        for (int i = 0; i < cartas.size(); i++) {
            if (cartas.get(i).elegida) {
                elegidos.add(i);
            }
        }
        if (elegidos.size() < minimo || elegidos.size() > maximo) {
            vista.mostrarMensaje("Elegí entre " + minimo + " y " + maximo + " personajes.");
            return;
        }
        vista.getControlador().onEquipoElegido(elegidos);
    }

    /** Carta clicable de un personaje: sprite, estadísticas y habilidades. */
    private class Carta extends JComponent {

        private final UnidadDTO u;
        private boolean elegida;
        private boolean encima;

        Carta(UnidadDTO u) {
            this.u = u;
            setPreferredSize(new Dimension(220, 400));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!elegida && elegidos() >= maximo) {
                        return;
                    }
                    elegida = !elegida;
                    actualizarContador();
                    PanelSeleccion.this.repaint();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Estilo.calidad(g);
            int w = getWidth();
            Estilo.caja(g, 0, 0, w, getHeight(), Estilo.PANEL,
                    elegida ? Estilo.ORO : encima ? Estilo.TOXICO : Estilo.BORDE);

            BufferedImage img = Sprites.de(u);
            if (img != null) {
                int sh = 190;
                int sw = img.getWidth() * sh / img.getHeight();
                g.setColor(new Color(0, 0, 0, 110));
                g.fillOval(w / 2 - sw / 2, 14 + sh - 8, sw, 16);
                g.drawImage(img, w / 2 - sw / 2, 14, sw, sh, null);
            }
            g.setFont(Estilo.fuente(17f));
            Estilo.textoCentrado(g, u.nombre(), w / 2, 232, elegida ? Estilo.ORO : Estilo.TEXTO);
            g.setFont(Estilo.fuente(12f));
            Estilo.textoCentrado(g, u.tipo(), w / 2, 249, Estilo.TEXTO_TENUE);

            int bx = 18;
            int bw = w - 36;
            g.setFont(Estilo.fuente(11f));
            Estilo.texto(g, "VIDA " + u.vidaMaxima(), bx, 270, Estilo.TEXTO);
            Estilo.barra(g, bx, 275, bw, 7, Math.min(1, u.vidaMaxima() / 150.0), Estilo.VIDA, Estilo.VIDA_FONDO);
            Estilo.texto(g, "MANÁ " + u.manaMaximo(), bx, 298, Estilo.TEXTO);
            Estilo.barra(g, bx, 303, bw, 7, Math.min(1, u.manaMaximo() / 100.0), Estilo.MANA, new Color(14, 22, 48));
            Estilo.texto(g, "ATK " + u.ataque() + "  DEF " + u.defensa() + "  VEL " + u.velocidad(), bx, 332, Estilo.TEXTO);

            int y = 354;
            for (HabilidadDTO h : u.habilidades()) {
                Estilo.texto(g, h.nombre() + " (" + h.costoMana() + ")", bx, y, Estilo.ORO);
                y += 16;
            }
            if (elegida) {
                g.setFont(Estilo.fuente(13f));
                Estilo.texto(g, "EN EL EQUIPO", w - 124, 24, Estilo.TOXICO);
            }
            g.dispose();
        }
    }
}
