package rpg.vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Paleta y primitivas de dibujo compartidas por todas las pantallas (estilo retro post-apocalíptico). */
final class Estilo {

    static final Color FONDO = new Color(16, 14, 20);
    static final Color PANEL = new Color(30, 27, 36);
    static final Color PANEL_CLARO = new Color(48, 43, 56);
    static final Color BORDE = new Color(96, 88, 110);
    static final Color TOXICO = new Color(140, 220, 60);
    static final Color ORO = new Color(244, 196, 64);
    static final Color TEXTO = new Color(232, 226, 210);
    static final Color TEXTO_TENUE = new Color(150, 144, 160);
    static final Color VIDA = new Color(76, 190, 84);
    static final Color VIDA_ENEMIGO = new Color(214, 70, 60);
    static final Color VIDA_FONDO = new Color(48, 18, 22);
    static final Color MANA = new Color(70, 140, 235);
    static final Color INFECTADO = new Color(176, 90, 230);
    static final Color PELIGRO = new Color(230, 80, 70);

    private Estilo() {
    }

    static Font fuente(float tamano) {
        return new Font(Font.MONOSPACED, Font.BOLD, Math.round(tamano));
    }

    static void calidad(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    /** Caja con borde biselado de "píxel gordo". */
    static void caja(Graphics2D g, int x, int y, int w, int h, Color relleno, Color borde) {
        g.setColor(Color.BLACK);
        g.fillRect(x, y, w, h);
        g.setColor(borde);
        g.fillRect(x + 2, y + 2, w - 4, h - 4);
        g.setColor(relleno);
        g.fillRect(x + 4, y + 4, w - 8, h - 8);
        g.setColor(new Color(255, 255, 255, 22));
        g.fillRect(x + 4, y + 4, w - 8, 2);
    }

    static void texto(Graphics2D g, String s, int x, int y, Color c) {
        g.setColor(Color.BLACK);
        g.drawString(s, x + 2, y + 2);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    static void textoCentrado(Graphics2D g, String s, int cx, int y, Color c) {
        FontMetrics fm = g.getFontMetrics();
        texto(g, s, cx - fm.stringWidth(s) / 2, y, c);
    }

    /** Barra rectangular sin degradados: borde negro, fondo oscuro y relleno plano. */
    static void barra(Graphics2D g, int x, int y, int w, int h, double fraccion, Color color, Color fondo) {
        g.setColor(Color.BLACK);
        g.fillRect(x - 1, y - 1, w + 2, h + 2);
        g.setColor(fondo);
        g.fillRect(x, y, w, h);
        int llenos = (int) Math.round(w * Math.max(0, Math.min(1, fraccion)));
        g.setColor(color);
        g.fillRect(x, y, llenos, h);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(x, y, llenos, Math.max(1, h / 3));
    }
}
