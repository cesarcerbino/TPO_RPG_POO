package rpg.vista;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import rpg.dto.EquipableDTO;
import rpg.dto.EquipamientoDTO;
import rpg.dto.UnidadDTO;

/**
 * Pantalla entre batallas para cambiar armas y armaduras: arriba el equipo (clic para elegir a quién),
 * abajo la mochila (clic en una pieza para ponérsela al elegido; lo que tenía vuelve a la mochila).
 */
public class PanelEquipamiento extends PanelConFondo {

    private final VistaSwing vista;
    private final JPanel filaEquipo = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
    private final ListaDeBloques mochila = new ListaDeBloques();
    private final JLabel ayuda = new JLabel();
    private EquipamientoDTO datos;
    private int seleccionado = -1;

    public PanelEquipamiento(VistaSwing vista) {
        super(170);
        this.vista = vista;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel arriba = new JPanel(new GridLayout(2, 1, 0, 4));
        arriba.setOpaque(false);
        JLabel titulo = new JLabel("EQUIPAMIENTO");
        titulo.setFont(Estilo.fuente(26f));
        titulo.setForeground(Estilo.TEXTO);
        ayuda.setFont(Estilo.fuente(13f));
        ayuda.setForeground(Estilo.TEXTO_TENUE);
        arriba.add(titulo);
        arriba.add(ayuda);
        add(arriba, BorderLayout.NORTH);

        filaEquipo.setOpaque(false);
        JScrollPane scroll = new JScrollPane(mochila, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(filaEquipo, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        botones.setOpaque(false);
        BotonPixel volver = new BotonPixel("Volver");
        volver.addActionListener(e -> vista.volverAlResultado());
        BotonPixel siguiente = new BotonPixel("Siguiente batalla");
        siguiente.setPreferredSize(new Dimension(230, 46));
        siguiente.addActionListener(e -> vista.getControlador().onContinuar());
        botones.add(volver);
        botones.add(siguiente);
        add(botones, BorderLayout.SOUTH);
    }

    public void mostrar(EquipamientoDTO e) {
        datos = e;
        // Se conserva al elegido entre actualizaciones; si ya no se puede (cayó), pasa al primero vivo.
        if (seleccionado < 0 || seleccionado >= e.equipo().size() || !e.equipo().get(seleccionado).vivo()) {
            seleccionado = -1;
            for (UnidadDTO u : e.equipo()) {
                if (u.vivo()) {
                    seleccionado = u.indice();
                    break;
                }
            }
        }
        filaEquipo.removeAll();
        for (UnidadDTO u : e.equipo()) {
            filaEquipo.add(new TarjetaMiembro(u));
        }
        mochila.removeAll();
        for (EquipableDTO eq : e.mochila()) {
            mochila.add(new BloqueEquipable(eq));
        }
        actualizarAyuda();
        revalidate();
        repaint();
    }

    private void actualizarAyuda() {
        if (datos.mochila().isEmpty()) {
            ayuda.setText("La mochila está vacía. Ganá batallas para encontrar armas y armaduras.");
        } else if (seleccionado >= 0) {
            ayuda.setText("Elegí una pieza de la mochila para equipar a " + datos.equipo().get(seleccionado).nombre() + ".");
        } else {
            ayuda.setText("No queda nadie en pie para equipar.");
        }
    }

    /** "+3 ATK  -2 DEF": solo los bonus que no son cero. */
    static String bonus(EquipableDTO e) {
        StringBuilder s = new StringBuilder();
        if (e.bonusAtaque() != 0) {
            s.append(String.format("%+d ATK", e.bonusAtaque()));
        }
        if (e.bonusDefensa() != 0) {
            if (s.length() > 0) {
                s.append("  ");
            }
            s.append(String.format("%+d DEF", e.bonusDefensa()));
        }
        return s.toString();
    }

    /** Componente clicable con resalte al pasar el mouse. */
    private abstract static class Clicable extends JComponent {

        boolean encima;

        Clicable(int ancho, int alto, boolean activo) {
            setPreferredSize(new Dimension(ancho, alto));
            if (!activo) {
                return;
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    alClic();
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

        abstract void alClic();

        abstract void dibujar(Graphics2D g, int w, int h);

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Estilo.calidad(g);
            dibujar(g, getWidth(), getHeight());
            g.dispose();
        }
    }

    /** Retrato, stats y lo que tiene puesto un miembro. El elegido se marca en verde; los caídos no se eligen. */
    private class TarjetaMiembro extends Clicable {

        private static final int TAM_RETRATO = 64;
        private final UnidadDTO u;

        TarjetaMiembro(UnidadDTO u) {
            super(214, 200, u.vivo());
            this.u = u;
        }

        @Override
        void alClic() {
            seleccionado = u.indice();
            actualizarAyuda();
            filaEquipo.repaint();
        }

        @Override
        void dibujar(Graphics2D g, int w, int h) {
            boolean elegido = u.indice() == seleccionado;
            Color borde = !u.vivo() ? new Color(70, 40, 44) : elegido ? Estilo.TOXICO : encima ? Estilo.ORO : Estilo.BORDE;
            Estilo.caja(g, 0, 0, w, h, u.vivo() ? Estilo.PANEL_CLARO : new Color(30, 24, 30), borde);
            int cx = w / 2;

            int ry = 10;
            BufferedImage img = Sprites.de(u);
            if (img != null) {
                double escala = Math.min(TAM_RETRATO / (double) img.getWidth(), TAM_RETRATO / (double) img.getHeight());
                int iw = (int) (img.getWidth() * escala);
                int ih = (int) (img.getHeight() * escala);
                Graphics2D gi = (Graphics2D) g.create();
                if (!u.vivo()) {
                    gi.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                }
                gi.drawImage(img, cx - iw / 2, ry + TAM_RETRATO - ih, iw, ih, null);
                gi.dispose();
            }

            int y = ry + TAM_RETRATO + 18;
            g.setFont(Estilo.fuente(14f));
            Estilo.textoCentrado(g, u.nombre().toUpperCase(), cx, y, u.vivo() ? Estilo.TEXTO : Estilo.TEXTO_TENUE);
            y += 16;
            g.setFont(Estilo.fuente(11f));
            String sub = u.vivo() ? "ATK " + u.ataque() + "   DEF " + u.defensa() : "CAÍDO";
            Estilo.textoCentrado(g, sub, cx, y, u.vivo() ? Estilo.ORO : Estilo.PELIGRO);

            y += 22;
            g.setFont(Estilo.fuente(10f));
            if (u.equipados().isEmpty()) {
                Estilo.textoCentrado(g, "Sin equipo", cx, y, Estilo.TEXTO_TENUE);
                return;
            }
            for (EquipableDTO e : u.equipados()) {
                Estilo.texto(g, e.ranura(), 14, y, Estilo.TEXTO_TENUE);
                y += 13;
                Estilo.texto(g, e.nombre() + "  " + bonus(e), 14, y, Estilo.TEXTO);
                y += 18;
            }
        }
    }

    /** Pieza de la mochila: ícono según la ranura, nombre, bonus y descripción. */
    private class BloqueEquipable extends Clicable {

        private final EquipableDTO e;

        BloqueEquipable(EquipableDTO e) {
            super(380, 78, true);
            this.e = e;
        }

        @Override
        void alClic() {
            if (seleccionado >= 0) {
                vista.getControlador().onEquipar(seleccionado, e.indice());
            }
        }

        @Override
        void dibujar(Graphics2D g, int w, int h) {
            Estilo.caja(g, 0, 0, w, h, Estilo.PANEL, encima ? Estilo.TOXICO : Estilo.BORDE);
            int lado = 50;
            int ix = 12;
            int iy = (h - lado) / 2;
            Estilo.caja(g, ix, iy, lado, lado, new Color(20, 18, 26), Estilo.BORDE);
            IconosDeObjeto.dibujar(g, e.ranura(), ix + 7, iy + 7, 3);

            int tx = ix + lado + 14;
            g.setFont(Estilo.fuente(14f));
            Estilo.texto(g, e.nombre().toUpperCase(), tx, 26, Estilo.ORO);
            g.setFont(Estilo.fuente(11f));
            Estilo.texto(g, e.ranura() + "  " + bonus(e), tx, 44, Estilo.TOXICO);
            g.setFont(Estilo.fuente(10f));
            Estilo.texto(g, e.descripcion(), tx, 60, Estilo.TEXTO_TENUE);
        }
    }
}
