package rpg.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import rpg.dto.UnidadDTO;

/**
 * Ficha de la unidad que está jugando su turno. Todo va centrado; primero lo más importante
 * (vida y maná con barras grandes) y abajo ataque, defensa, velocidad y experiencia.
 */
class FichaDeStats extends JPanel {

    private static final int MARGEN = 12;
    private static final int ALTO_NOMBRE = 18;
    private static final int ALTO_SUBTITULO = 14;
    private static final int ALTO_ETIQUETAS = 16;
    private static final int ALTO_BARRA = 25;
    private static final int ALTO_STATS = 24;
    private static final int ESPACIO = 5;

    private static final Color FONDO_FICHA = new Color(20, 18, 26);
    private static final Color FONDO_MANA = new Color(16, 28, 52);

    private UnidadDTO unidad;

    FichaDeStats() {
        setOpaque(false);
        setPreferredSize(new Dimension(280, 0));
    }

    void mostrar(UnidadDTO u) {
        this.unidad = u;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.calidad(g);
        int w = getWidth();
        int h = getHeight();
        Estilo.caja(g, 0, 0, w, h, FONDO_FICHA, Estilo.BORDE);
        if (unidad == null) {
            g.dispose();
            return;
        }
        UnidadDTO u = unidad;
        Color acento = u.esPersonaje() ? Estilo.TOXICO : Estilo.PELIGRO;
        g.setColor(acento);
        g.fillRect(4, 4, w - 8, 3);

        List<String> estados = new ArrayList<>();
        if (u.infectado()) {
            estados.add("INFECTADO");
        }
        if (u.turnosBloqueado() > 0) {
            estados.add("BLOQUEADO " + u.turnosBloqueado());
        }
        if (u.defendiendo()) {
            estados.add("DEFIENDE");
        }
        boolean tieneMana = u.manaMaximo() > 0;

        // se calcula el alto total para centrar el contenido en vertical
        int alto = ALTO_NOMBRE + ALTO_SUBTITULO + (estados.isEmpty() ? 0 : ALTO_ETIQUETAS)
                + ESPACIO + ALTO_BARRA + (tieneMana ? ESPACIO + ALTO_BARRA : 0) + ESPACIO + ALTO_STATS;
        int y = Math.max(8, (h - alto) / 2 + 2);
        int cx = w / 2;
        int x = MARGEN;
        int ancho = w - 2 * MARGEN;

        // encabezado: nombre, clase/nivel y estados
        g.setFont(Estilo.fuente(15f));
        Estilo.textoCentrado(g, u.nombre().toUpperCase(), cx, y + 14, Estilo.TEXTO);
        y += ALTO_NOMBRE;
        g.setFont(Estilo.fuente(11f));
        String subtitulo = u.esPersonaje() ? u.tipo().toUpperCase() + "  ·  NV " + u.nivel() : "ENEMIGO";
        Estilo.textoCentrado(g, subtitulo, cx, y + 11, Estilo.TEXTO_TENUE);
        y += ALTO_SUBTITULO;
        if (!estados.isEmpty()) {
            etiquetasCentradas(g, estados, cx, y + 2);
            y += ALTO_ETIQUETAS;
        }
        y += ESPACIO;

        // lo más importante: vida y maná
        double fVida = fraccion(u.vida(), u.vidaMaxima());
        Color colorVida = !u.esPersonaje() ? Estilo.VIDA_ENEMIGO : fVida <= 0.3 ? Estilo.PELIGRO : Estilo.VIDA;
        barra(g, "VIDA", u.vida(), u.vidaMaxima(), x, y, ancho, colorVida, Estilo.VIDA_FONDO);
        y += ALTO_BARRA;
        if (tieneMana) {
            y += ESPACIO;
            barra(g, "MANÁ", u.mana(), u.manaMaximo(), x, y, ancho, Estilo.MANA, FONDO_MANA);
            y += ALTO_BARRA;
        }
        y += ESPACIO;

        // stats secundarias
        List<String[]> stats = new ArrayList<>();
        stats.add(new String[] {"ATK", String.valueOf(u.ataque())});
        stats.add(new String[] {"DEF", String.valueOf(u.defensa())});
        stats.add(new String[] {"VEL", String.valueOf(u.velocidad())});
        if (u.esPersonaje()) {
            stats.add(new String[] {"XP", u.experiencia() + "/" + u.experienciaParaSubir()});
        }
        int n = stats.size();
        int celda = (ancho - (n - 1) * ESPACIO) / n;
        int sx = cx - (n * celda + (n - 1) * ESPACIO) / 2;
        for (String[] s : stats) {
            celda(g, s[0], s[1], sx, y, celda);
            sx += celda + ESPACIO;
        }
        g.dispose();
    }

    private static double fraccion(int valor, int maximo) {
        return maximo <= 0 ? 0 : (double) valor / maximo;
    }

    /** Rótulo a la izquierda y valor a la derecha, con la barra segmentada debajo. */
    private static void barra(Graphics2D g, String nombre, int valor, int maximo, int x, int y, int ancho,
                              Color color, Color fondo) {
        g.setFont(Estilo.fuente(11f));
        Estilo.texto(g, nombre, x, y + 10, color);
        String texto = valor + " / " + maximo;
        FontMetrics fm = g.getFontMetrics();
        Estilo.texto(g, texto, x + ancho - fm.stringWidth(texto) - 2, y + 10, Estilo.TEXTO);
        int by = y + 13;
        int alto = 11;
        Estilo.barra(g, x, by, ancho, alto, fraccion(valor, maximo), color, fondo);
        g.setColor(new Color(0, 0, 0, 90));
        for (int i = 1; i < 10; i++) {
            g.fillRect(x + ancho * i / 10, by, 1, alto);
        }
    }

    private static void celda(Graphics2D g, String nombre, String valor, int x, int y, int ancho) {
        g.setColor(Color.BLACK);
        g.fillRect(x, y, ancho, ALTO_STATS);
        g.setColor(Estilo.PANEL_CLARO);
        g.fillRect(x + 2, y + 2, ancho - 4, ALTO_STATS - 4);
        int cx = x + ancho / 2;
        g.setFont(Estilo.fuente(9f));
        Estilo.textoCentrado(g, nombre, cx, y + 10, Estilo.TEXTO_TENUE);
        g.setFont(Estilo.fuente(11f));
        Estilo.textoCentrado(g, valor, cx, y + 21, Estilo.ORO);
    }

    private static void etiquetasCentradas(Graphics2D g, List<String> etiquetas, int cx, int y) {
        g.setFont(Estilo.fuente(10f));
        FontMetrics fm = g.getFontMetrics();
        int total = 0;
        for (String e : etiquetas) {
            total += fm.stringWidth(e) + 8;
        }
        total += (etiquetas.size() - 1) * 4;
        int x = cx - total / 2;
        for (String e : etiquetas) {
            Color c = e.equals("INFECTADO") ? Estilo.INFECTADO : Estilo.ORO;
            int ancho = fm.stringWidth(e) + 8;
            g.setColor(Color.BLACK);
            g.fillRect(x, y, ancho, 13);
            g.setColor(c);
            g.drawRect(x, y, ancho - 1, 12);
            g.drawString(e, x + 4, y + 10);
            x += ancho + 4;
        }
    }
}
