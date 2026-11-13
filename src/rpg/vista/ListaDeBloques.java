package rpg.vista;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * Panel de bloques que acompaña el ancho del scroll y calcula su alto según cuántas filas ocupan
 * (un FlowLayout común no lo hace y la última fila quedaba cortada sin barra de scroll).
 */
class ListaDeBloques extends JPanel implements Scrollable {

    private static final int HUECO = 18;

    ListaDeBloques() {
        super(new FlowLayout(FlowLayout.CENTER, HUECO, HUECO));
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        int ancho = getParent() != null ? getParent().getWidth() : 0;
        if (ancho <= 0) {
            return super.getPreferredSize();
        }
        int x = HUECO;
        int y = HUECO;
        int filaAlto = 0;
        for (Component c : getComponents()) {
            Dimension d = c.getPreferredSize();
            if (x > HUECO && x + d.width + HUECO > ancho) {
                x = HUECO;
                y += filaAlto + HUECO;
                filaAlto = 0;
            }
            x += d.width + HUECO;
            filaAlto = Math.max(filaAlto, d.height);
        }
        return new Dimension(ancho, y + filaAlto + HUECO);
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(24, r.height - 40); }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
