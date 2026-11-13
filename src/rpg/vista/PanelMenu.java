package rpg.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

public class PanelMenu extends PanelConFondo {

    public PanelMenu(VistaSwing vista) {
        super(110);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.NONE;

        add(new Titulo(), c);

        c.insets = new Insets(26, 10, 8, 10);
        BotonPixel nueva = boton("Nueva partida", e -> vista.getControlador().onNuevaPartida());
        add(nueva, c);
        c.insets = new Insets(8, 10, 8, 10);
        add(boton("Cargar partida", e -> vista.getControlador().onCargarPartida()), c);
        add(boton("Salir", e -> vista.getControlador().onSalir()), c);
    }

    private static BotonPixel boton(String texto, java.awt.event.ActionListener al) {
        BotonPixel b = new BotonPixel(texto);
        b.setFont(Estilo.fuente(18f));
        b.setPreferredSize(new Dimension(280, 54));
        b.addActionListener(al);
        return b;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        Estilo.calidad(g);
        int w = getWidth();
        int h = getHeight();
        double f = h * 0.34 / 300.0;
        String[] bandoA = {"bombero", "militar", "medico", "ingeniero"};
        String[] bandoB = {"zombie_comun", "zombie_tanque", "infectado_rapido"};
        for (int i = 0; i < bandoA.length; i++) {
            dibujar(g, Sprites.cargar(bandoA[i] + "_0"), (int) (w * 0.04 + i * w * 0.075), h - 24, f, false);
        }
        for (int i = 0; i < bandoB.length; i++) {
            dibujar(g, Sprites.cargar(bandoB[i]), (int) (w * 0.96 - (bandoB.length - i) * w * 0.095), h - 24, f, true);
        }
        g.dispose();
    }

    private static void dibujar(Graphics2D g, BufferedImage img, int x, int base, double f, boolean espejo) {
        if (img == null) {
            return;
        }
        int sw = (int) (img.getWidth() * f);
        int sh = (int) (img.getHeight() * f);
        g.setColor(new Color(0, 0, 0, 110));
        g.fillOval(x, base - 8, sw, 16);
        if (espejo) {
            g.drawImage(img, x + sw, base - sh, -sw, sh, null);
        } else {
            g.drawImage(img, x, base - sh, sw, sh, null);
        }
    }

    /** Título grande con sombra, dibujado a mano para no depender de una tipografía externa. */
    private static class Titulo extends JComponent {

        Titulo() {
            setPreferredSize(new Dimension(760, 150));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Estilo.calidad(g);
            int cx = getWidth() / 2;
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 62));
            Estilo.textoCentrado(g, "SOBREVIVIENTES", cx, 62, Estilo.TEXTO);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
            Estilo.textoCentrado(g, "VS. ZOMBIES", cx, 112, Estilo.TOXICO);
            g.setFont(Estilo.fuente(14f));
            Estilo.textoCentrado(g, "RPG por turnos  -  Grupo 7", cx, 140, Estilo.TEXTO_TENUE);
            g.dispose();
        }
    }
}
