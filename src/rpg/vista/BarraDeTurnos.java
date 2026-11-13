package rpg.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import rpg.dto.UnidadDTO;

/** Franja con el orden de turnos: el que actúa ahora (resaltado) y los que vienen después. */
class BarraDeTurnos extends JPanel {

    private static final int ALTO = 76;
    private static final int ANCHO_MAX_CASILLA = 128;
    private static final int ANCHO_MIN_CASILLA = 112;
    private static final int SEPARACION = 8;
    private static final int RETRATO = 44;

    private List<UnidadDTO> turnos = new ArrayList<>();

    BarraDeTurnos() {
        setPreferredSize(new Dimension(0, ALTO));
    }

    void mostrar(List<UnidadDTO> turnos) {
        this.turnos = turnos;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Estilo.calidad(g);
        int w = getWidth();
        int h = getHeight();
        g.setColor(Estilo.FONDO);
        g.fillRect(0, 0, w, h);
        g.setColor(Estilo.BORDE);
        g.fillRect(0, h - 3, w, 3);

        g.setFont(Estilo.fuente(12f));
        Estilo.texto(g, "TURNOS", 16, h / 2 + 4, Estilo.TEXTO_TENUE);
        int x = 16 + g.getFontMetrics().stringWidth("TURNOS") + 16;

        if (!turnos.isEmpty()) {
            int disponible = w - x - 16;
            // si la ventana es angosta se muestran menos turnos en lugar de recortar los nombres
            int entran = Math.max(1, (disponible + SEPARACION) / (ANCHO_MIN_CASILLA + SEPARACION));
            int visibles = Math.min(turnos.size(), entran);
            int ancho = Math.min(ANCHO_MAX_CASILLA, (disponible - SEPARACION * (visibles - 1)) / visibles);
            int alto = h - 18;
            for (int i = 0; i < visibles; i++) {
                dibujarCasilla(g, turnos.get(i), i, x, 7, ancho, alto);
                x += ancho + SEPARACION;
            }
        }
        g.dispose();
    }

    private void dibujarCasilla(Graphics2D g, UnidadDTO u, int posicion, int x, int y, int ancho, int alto) {
        boolean esActual = posicion == 0;
        Color borde = esActual ? Estilo.ORO : u.esPersonaje() ? Estilo.VIDA : Estilo.VIDA_ENEMIGO;
        Estilo.caja(g, x, y, ancho, alto, esActual ? Estilo.PANEL_CLARO : Estilo.PANEL, borde);

        int lado = Math.min(RETRATO, alto - 12);
        int rx = x + 6;
        int ry = y + (alto - lado) / 2;
        dibujarRetrato(g, u, rx, ry, lado);

        int tx = rx + lado + 6;
        int anchoTexto = x + ancho - 6 - tx;
        if (anchoTexto < 12) {
            return;
        }
        g.setFont(Estilo.fuente(10f));
        String etiqueta = esActual ? "AHORA" : (posicion + 1) + "°";
        Estilo.texto(g, recortar(g, etiqueta, anchoTexto), tx, y + alto / 2 - 4, borde);
        Color colorNombre = u.infectado() ? Estilo.INFECTADO : Estilo.TEXTO;
        Estilo.texto(g, recortar(g, nombreCorto(u), anchoTexto), tx, y + alto / 2 + 11, colorNombre);
    }

    /** "Zombie Rápido A" → "Rápido A": el retrato ya muestra que es un zombie y la letra es lo que los distingue. */
    private static String nombreCorto(UnidadDTO u) {
        String n = u.nombre();
        return !u.esPersonaje() && n.startsWith("Zombie ") ? n.substring("Zombie ".length()) : n;
    }

    /** Recorta la parte de arriba del sprite (la cabeza) como retrato cuadrado. */
    private static void dibujarRetrato(Graphics2D g, UnidadDTO u, int x, int y, int lado) {
        g.setColor(new Color(12, 12, 16));
        g.fillRect(x, y, lado, lado);
        BufferedImage sprite = Sprites.de(u);
        if (sprite == null) {
            g.setColor(u.esPersonaje() ? new Color(70, 130, 90) : new Color(130, 60, 60));
            g.fillRect(x + 4, y + 4, lado - 8, lado - 8);
            return;
        }
        int recorte = Math.min(sprite.getWidth(), (int) (sprite.getHeight() * 0.34));
        int sx = (sprite.getWidth() - recorte) / 2;
        g.drawImage(sprite, x, y, x + lado, y + lado, sx, 0, sx + recorte, recorte, null);
    }

    private static String recortar(Graphics2D g, String s, int ancho) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(s) <= ancho) {
            return s;
        }
        String r = s;
        while (r.length() > 1 && fm.stringWidth(r + "..") > ancho) {
            r = r.substring(0, r.length() - 1);
        }
        return r + "..";
    }
}
