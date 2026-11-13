package rpg.vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

/** Botón con aspecto retro: caja biselada, resalte al pasar el mouse y hundido al presionar. */
class BotonPixel extends JButton {

    private boolean encima;

    BotonPixel(String texto) {
        super(texto);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFont(Estilo.fuente(15f));
        setForeground(Estilo.TEXTO);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(150, 46));
        addMouseListener(new MouseAdapter() {
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
        boolean activo = isEnabled();
        boolean presionado = activo && getModel().isPressed();
        Color borde = !activo ? Estilo.BORDE.darker() : encima ? Estilo.TOXICO : Estilo.BORDE;
        Color relleno = !activo ? Estilo.PANEL : presionado ? Estilo.FONDO : encima ? Estilo.PANEL_CLARO.brighter() : Estilo.PANEL_CLARO;
        int desplazamiento = presionado ? 2 : 0;
        Estilo.caja(g, 0, desplazamiento, getWidth(), getHeight() - desplazamiento, relleno, borde);
        g.setFont(getFont());
        Color texto = !activo ? Estilo.TEXTO_TENUE.darker() : encima ? Estilo.ORO : Estilo.TEXTO;
        int y = (getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2 + desplazamiento;
        Estilo.textoCentrado(g, getText(), getWidth() / 2, y, texto);
        g.dispose();
    }
}
