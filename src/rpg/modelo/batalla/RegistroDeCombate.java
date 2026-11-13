package rpg.modelo.batalla;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class RegistroDeCombate implements Serializable {

    private final List<EventoCombate> eventos = new ArrayList<>();
    // Los observadores no se guardan en disco (el controlador los vuelve a suscribir).
    private transient List<ObservadorDeCombate> observadores;

    public void agregarObservador(ObservadorDeCombate o) {
        if (observadores == null) {
            observadores = new ArrayList<>();
        }
        observadores.add(o);
    }

    public void registrar(TipoEvento tipo, String texto) {
        EventoCombate e = new EventoCombate(tipo, texto);
        eventos.add(e);
        if (observadores != null) {
            for (ObservadorDeCombate o : observadores) {
                o.alRegistrarse(e);
            }
        }
    }

    public void registrar(String texto) {
        registrar(TipoEvento.SISTEMA, texto);
    }

    public List<String> getHistorial() {
        List<String> lineas = new ArrayList<>();
        for (EventoCombate e : eventos) {
            lineas.add(e.getTexto());
        }
        return lineas;
    }
}
