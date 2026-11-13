package rpg;

import javax.swing.SwingUtilities;

import rpg.controlador.ControladorJuego;
import rpg.vista.VistaSwing;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VistaSwing vista = new VistaSwing();
            ControladorJuego controlador = new ControladorJuego(vista);
            vista.setControlador(controlador);
            vista.setVisible(true);
            controlador.iniciar();
        });
    }
}
