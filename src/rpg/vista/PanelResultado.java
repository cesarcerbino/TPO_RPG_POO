package rpg.vista;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import rpg.dto.EquipableDTO;
import rpg.dto.ObjetoDTO;
import rpg.dto.PartidaDTO;
import rpg.dto.ResultadoBatallaDTO;
import rpg.dto.UnidadDTO;

/** Pantalla entre batallas: título, resumen, una tarjeta por miembro del equipo y el botín conseguido. */
public class PanelResultado extends PanelConFondo {

    private static final int ANCHO = 900;

    private final Encabezado encabezado = new Encabezado();
    private final Chips chips = new Chips();
    private final Seccion seccionEquipo = new Seccion("EQUIPO");
    private final Seccion seccionBotin = new Seccion("BOTÍN");
    private final JPanel filaEquipo = fila(14);
    private final JPanel filaBotin = fila(14);
    private final BotonPixel btnGuardar = new BotonPixel("Guardar partida");
    private final BotonPixel btnGuardarYSalir = new BotonPixel("Guardar y salir al menú");
    private final BotonPixel btnEquipamiento = new BotonPixel("Equipamiento");
    private final BotonPixel btnContinuar = new BotonPixel("");

    public PanelResultado(VistaSwing vista) {
        super(170);
        setLayout(new GridBagLayout());

        JPanel caja = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Estilo.caja((Graphics2D) g, 0, 0, getWidth(), getHeight(), Estilo.PANEL, Estilo.BORDE);
            }
        };
        caja.setOpaque(false);
        caja.setBorder(BorderFactory.createEmptyBorder(22, 30, 26, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(0, 0, 6, 0);
        caja.add(encabezado, c);
        c.insets = new Insets(0, 0, 16, 0);
        caja.add(chips, c);
        c.insets = new Insets(0, 0, 8, 0);
        caja.add(seccionEquipo, c);
        c.insets = new Insets(0, 0, 16, 0);
        caja.add(filaEquipo, c);
        c.insets = new Insets(0, 0, 8, 0);
        caja.add(seccionBotin, c);
        c.insets = new Insets(0, 0, 20, 0);
        caja.add(filaBotin, c);

        JPanel botones = fila(14);
        btnContinuar.setPreferredSize(new Dimension(210, 46));
        btnGuardar.setPreferredSize(new Dimension(190, 46));
        btnGuardarYSalir.setPreferredSize(new Dimension(270, 46));
        btnEquipamiento.setPreferredSize(new Dimension(170, 46));
        botones.add(btnGuardar);
        botones.add(btnGuardarYSalir);
        botones.add(btnEquipamiento);
        botones.add(btnContinuar);
        c.insets = new Insets(0, 0, 0, 0);
        caja.add(botones, c);
        add(caja);

        btnGuardar.addActionListener(e -> vista.getControlador().onGuardar());
        btnGuardarYSalir.addActionListener(e -> vista.getControlador().onGuardarYVolverAlMenu());
        btnEquipamiento.addActionListener(e -> vista.getControlador().onAbrirEquipamiento());
        btnContinuar.addActionListener(e -> vista.getControlador().onContinuar());
    }

    private static JPanel fila(int separacion) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, separacion, 0));
        p.setOpaque(false);
        return p;
    }

    public void mostrar(ResultadoBatallaDTO r, PartidaDTO p) {
        // Los niveles no terminan: la partida solo se acaba cuando cae el equipo.
        boolean gano = r.victoria();
        String titulo = gano ? "¡BATALLA GANADA!" : "DERROTA";
        String subtitulo = gano ? "NIVEL " + p.nivel() : "EL EQUIPO RESISTIÓ HASTA EL NIVEL " + p.nivel();
        encabezado.mostrar(titulo, subtitulo, gano ? Estilo.TOXICO : Estilo.PELIGRO);

        long vivos = r.equipo().stream().filter(UnidadDTO::vivo).count();
        List<String[]> datos = new ArrayList<>();
        datos.add(new String[] {"TURNOS", String.valueOf(r.turnosUsados())});
        if (r.victoria()) {
            datos.add(new String[] {"EXPERIENCIA", "+" + r.experienciaGanada() + " c/u"});
        }
        datos.add(new String[] {"SOBREVIVIENTES", vivos + " / " + r.equipo().size()});
        chips.mostrar(datos);

        filaEquipo.removeAll();
        for (int i = 0; i < r.equipo().size(); i++) {
            int subidos = i < r.nivelesSubidos().size() ? r.nivelesSubidos().get(i) : 0;
            filaEquipo.add(new TarjetaMiembro(r.equipo().get(i), subidos, r.victoria() ? r.experienciaGanada() : 0));
        }

        filaBotin.removeAll();
        seccionBotin.setVisible(r.victoria());
        filaBotin.setVisible(r.victoria());
        if (r.botin().isEmpty() && r.equipables().isEmpty()) {
            filaBotin.add(new Nota("Esta vez no encontraron nada entre los escombros."));
        } else {
            for (ObjetoDTO o : r.botin()) {
                filaBotin.add(new TarjetaObjeto(o.nombre(), o.nombre(), o.descripcion()));
            }
            for (EquipableDTO e : r.equipables()) {
                filaBotin.add(new TarjetaObjeto(e.ranura(), e.nombre(), PanelEquipamiento.bonus(e) + ". " + e.descripcion()));
            }
        }
        revalidate();
        repaint();

        btnGuardar.setVisible(p.puedeGuardar());
        btnGuardarYSalir.setVisible(p.puedeGuardar());
        btnEquipamiento.setVisible(!p.terminada());
        btnContinuar.setText(p.terminada() ? "Volver al menú" : "Siguiente batalla");
    }

    // ---------------------------------------------------------------- componentes dibujados

    /** Componente transparente que se dibuja a mano con antialias y fuente del juego. */
    private abstract static class Dibujo extends JComponent {

        Dibujo(int ancho, int alto) {
            setPreferredSize(new Dimension(ancho, alto));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Estilo.calidad(g);
            dibujar(g, getWidth(), getHeight());
            g.dispose();
        }

        abstract void dibujar(Graphics2D g, int w, int h);
    }

    private static final class Encabezado extends Dibujo {

        private String titulo = "";
        private String subtitulo = "";
        private Color color = Estilo.TEXTO;

        Encabezado() {
            super(ANCHO, 82);
        }

        void mostrar(String titulo, String subtitulo, Color color) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.color = color;
            repaint();
        }

        @Override
        void dibujar(Graphics2D g, int w, int h) {
            int cx = w / 2;
            g.setFont(Estilo.fuente(42f));
            // sombra gruesa de "píxel" para que el título pese más
            g.setColor(Color.BLACK);
            g.drawString(titulo, cx - g.getFontMetrics().stringWidth(titulo) / 2 + 4, 48);
            Estilo.textoCentrado(g, titulo, cx, 44, color);

            g.setFont(Estilo.fuente(13f));
            FontMetrics fm = g.getFontMetrics();
            int ancho = fm.stringWidth(subtitulo);
            Estilo.textoCentrado(g, subtitulo, cx, 72, Estilo.TEXTO_TENUE);
            int y = 67;
            int largo = 120;
            g.setColor(color.darker());
            g.fillRect(cx - ancho / 2 - 16 - largo, y, largo, 3);
            g.fillRect(cx + ancho / 2 + 16, y, largo, 3);
            g.setColor(color);
            g.fillRect(cx - ancho / 2 - 12, y - 1, 5, 5);
            g.fillRect(cx + ancho / 2 + 7, y - 1, 5, 5);
        }
    }

    /** Fila de recuadros con un dato cada uno: rótulo chico arriba y valor grande abajo. */
    private static final class Chips extends Dibujo {

        private static final int ANCHO_CHIP = 190;
        private static final int SEPARACION = 14;
        private List<String[]> datos = List.of();

        Chips() {
            super(ANCHO, 50);
        }

        void mostrar(List<String[]> datos) {
            this.datos = datos;
            repaint();
        }

        @Override
        void dibujar(Graphics2D g, int w, int h) {
            int total = datos.size() * ANCHO_CHIP + (datos.size() - 1) * SEPARACION;
            int x = (w - total) / 2;
            for (String[] d : datos) {
                Estilo.caja(g, x, 0, ANCHO_CHIP, h, Estilo.PANEL_CLARO, Estilo.BORDE);
                int cx = x + ANCHO_CHIP / 2;
                g.setFont(Estilo.fuente(10f));
                Estilo.textoCentrado(g, d[0], cx, 19, Estilo.TEXTO_TENUE);
                g.setFont(Estilo.fuente(16f));
                Estilo.textoCentrado(g, d[1], cx, 39, Estilo.ORO);
                x += ANCHO_CHIP + SEPARACION;
            }
        }
    }

    /** Título de sección centrado entre dos líneas. */
    private static final class Seccion extends Dibujo {

        private final String texto;

        Seccion(String texto) {
            super(ANCHO, 18);
            this.texto = texto;
        }

        @Override
        void dibujar(Graphics2D g, int w, int h) {
            g.setFont(Estilo.fuente(12f));
            int ancho = g.getFontMetrics().stringWidth(texto);
            Estilo.textoCentrado(g, texto, w / 2, 13, Estilo.TEXTO);
            g.setColor(Estilo.BORDE);
            g.fillRect(0, 8, w / 2 - ancho / 2 - 14, 2);
            g.fillRect(w / 2 + ancho / 2 + 14, 8, w - (w / 2 + ancho / 2 + 14), 2);
        }
    }

    private static final class Nota extends Dibujo {

        private final String texto;

        Nota(String texto) {
            super(ANCHO, 30);
            this.texto = texto;
        }

        @Override
        void dibujar(Graphics2D g, int w, int h) {
            g.setFont(Estilo.fuente(13f));
            Estilo.textoCentrado(g, texto, w / 2, 20, Estilo.TEXTO_TENUE);
        }
    }

    /** Retrato, nivel, vida y experiencia de un miembro; resalta si subió de nivel y se apaga si cayó. */
    private static final class TarjetaMiembro extends Dibujo {

        private static final int TAM_RETRATO = 76;
        private final UnidadDTO u;
        private final int nivelesSubidos;
        private final int xpGanada;

        TarjetaMiembro(UnidadDTO u, int nivelesSubidos, int xpGanada) {
            super(204, 200);
            this.u = u;
            this.nivelesSubidos = nivelesSubidos;
            this.xpGanada = xpGanada;
        }

        @Override
        void dibujar(Graphics2D g, int w, int h) {
            boolean subio = u.vivo() && nivelesSubidos > 0;
            Color borde = !u.vivo() ? new Color(70, 40, 44) : subio ? Estilo.ORO : Estilo.BORDE;
            Estilo.caja(g, 0, 0, w, h, u.vivo() ? Estilo.PANEL_CLARO : new Color(30, 24, 30), borde);
            int cx = w / 2;

            if (subio) {
                g.setColor(Estilo.ORO);
                g.fillRect(4, 4, w - 8, 18);
                g.setFont(Estilo.fuente(11f));
                g.setColor(Color.BLACK);
                String cinta = nivelesSubidos > 1 ? "▲ +" + nivelesSubidos + " NIVELES" : "▲ ¡SUBIÓ DE NIVEL!";
                g.drawString(cinta, cx - g.getFontMetrics().stringWidth(cinta) / 2, 17);
            }

            // retrato sobre un piso oscuro
            int ry = 26;
            g.setColor(new Color(0, 0, 0, 70));
            g.fillOval(cx - 30, ry + TAM_RETRATO - 8, 60, 12);
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
            if (!u.vivo()) {
                sello(g, "CAÍDO", cx, ry + TAM_RETRATO / 2);
            }

            int y = ry + TAM_RETRATO + 18;
            g.setFont(Estilo.fuente(14f));
            Estilo.textoCentrado(g, u.nombre().toUpperCase(), cx, y, u.vivo() ? Estilo.TEXTO : Estilo.TEXTO_TENUE);
            y += 15;
            g.setFont(Estilo.fuente(11f));
            String nivel = u.tipo().toUpperCase() + "  ·  NV " + (subio ? (u.nivel() - nivelesSubidos) + " → " + u.nivel() : u.nivel());
            Estilo.textoCentrado(g, nivel, cx, y, subio ? Estilo.ORO : Estilo.TEXTO_TENUE);

            int x = 14;
            int ancho = w - 28;
            y += 10;
            if (!u.vivo()) {
                g.setFont(Estilo.fuente(11f));
                String texto = xpGanada > 0 ? "No recibe experiencia" : "Cayó en combate";
                Estilo.textoCentrado(g, texto, cx, y + 26, new Color(170, 90, 90));
                return;
            }
            barra(g, "VIDA", u.vida() + "/" + u.vidaMaxima(), x, y, ancho,
                    fraccion(u.vida(), u.vidaMaxima()), Estilo.VIDA, Estilo.VIDA_FONDO);
            y += 26;
            String rotuloXp = xpGanada > 0 ? "XP +" + xpGanada : "XP";
            barra(g, rotuloXp, u.experiencia() + "/" + u.experienciaParaSubir(), x, y, ancho,
                    fraccion(u.experiencia(), u.experienciaParaSubir()), Estilo.ORO, new Color(48, 40, 12));
        }

        private static void barra(Graphics2D g, String rotulo, String valor, int x, int y, int ancho,
                                  double fr, Color color, Color fondo) {
            g.setFont(Estilo.fuente(10f));
            Estilo.texto(g, rotulo, x, y + 9, color);
            FontMetrics fm = g.getFontMetrics();
            Estilo.texto(g, valor, x + ancho - fm.stringWidth(valor) - 2, y + 9, Estilo.TEXTO);
            Estilo.barra(g, x, y + 12, ancho, 8, fr, color, fondo);
        }

        private static void sello(Graphics2D g, String texto, int cx, int cy) {
            g.setFont(Estilo.fuente(18f));
            FontMetrics fm = g.getFontMetrics();
            int ancho = fm.stringWidth(texto) + 20;
            Graphics2D gs = (Graphics2D) g.create();
            gs.rotate(Math.toRadians(-12), cx, cy);
            gs.setColor(new Color(0, 0, 0, 150));
            gs.fillRect(cx - ancho / 2, cy - 15, ancho, 30);
            gs.setColor(Estilo.PELIGRO);
            gs.fillRect(cx - ancho / 2, cy - 15, ancho, 3);
            gs.fillRect(cx - ancho / 2, cy + 12, ancho, 3);
            gs.drawString(texto, cx - fm.stringWidth(texto) / 2, cy + 7);
            gs.dispose();
        }
    }

    /** Objeto o equipable de botín: ícono pixel art, nombre y descripción. */
    private static final class TarjetaObjeto extends Dibujo {

        private final String icono;
        private final String nombre;
        private final String descripcion;

        TarjetaObjeto(String icono, String nombre, String descripcion) {
            super(360, 70);
            this.icono = icono;
            this.nombre = nombre;
            this.descripcion = descripcion;
        }

        @Override
        void dibujar(Graphics2D g, int w, int h) {
            Estilo.caja(g, 0, 0, w, h, Estilo.PANEL_CLARO, Estilo.ORO);
            int lado = 50;
            int ix = 10;
            int iy = (h - lado) / 2;
            Estilo.caja(g, ix, iy, lado, lado, new Color(20, 18, 26), Estilo.BORDE);
            IconosDeObjeto.dibujar(g, icono, ix + 7, iy + 7, 3);

            int tx = ix + lado + 14;
            g.setFont(Estilo.fuente(15f));
            Estilo.texto(g, nombre.toUpperCase(), tx, 28, Estilo.ORO);
            g.setFont(Estilo.fuente(10f));
            int y = 44;
            for (String linea : partir(descripcion, g.getFontMetrics(), w - tx - 12)) {
                Estilo.texto(g, linea, tx, y, Estilo.TEXTO_TENUE);
                y += 13;
            }
        }

        private static List<String> partir(String texto, FontMetrics fm, int ancho) {
            List<String> lineas = new ArrayList<>();
            StringBuilder actual = new StringBuilder();
            for (String palabra : texto.split(" ")) {
                String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
                if (fm.stringWidth(prueba) > ancho && actual.length() > 0) {
                    lineas.add(actual.toString());
                    actual = new StringBuilder(palabra);
                } else {
                    actual = new StringBuilder(prueba);
                }
            }
            if (actual.length() > 0) {
                lineas.add(actual.toString());
            }
            return lineas;
        }
    }

    private static double fraccion(int valor, int maximo) {
        return maximo <= 0 ? 0 : (double) valor / maximo;
    }
}
