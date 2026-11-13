package rpg.vista;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/** Panel que pinta la imagen de la ciudad en ruinas (recortada para cubrir) con una capa oscura encima. */
class PanelConFondo extends JPanel {

    private final int oscuridad;

    PanelConFondo(int oscuridad) {
        this.oscuridad = oscuridad;
        setBackground(Estilo.FONDO);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        Estilo.calidad(g);
        dibujarFondo(g, getWidth(), getHeight());
        g.setColor(new Color(8, 6, 12, oscuridad));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.dispose();
    }

    static void dibujarFondo(Graphics2D g, int w, int h) {
        BufferedImage fondo = Sprites.fondo();
        if (fondo == null) {
            g.setPaint(new java.awt.GradientPaint(0, 0, new Color(70, 40, 30), 0, h, Estilo.FONDO));
            g.fillRect(0, 0, w, h);
            return;
        }
        double escala = Math.max(w / (double) fondo.getWidth(), h / (double) fondo.getHeight());
        int dw = (int) Math.ceil(fondo.getWidth() * escala);
        int dh = (int) Math.ceil(fondo.getHeight() * escala);
        g.drawImage(fondo, (w - dw) / 2, h - dh, dw, dh, null);
    }
}
