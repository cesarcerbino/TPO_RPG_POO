package rpg.controlador;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import rpg.dto.PartidaGuardadaDTO;
import rpg.modelo.partida.Partida;
import rpg.persistencia.Persistencia;

/**
 * Responsabilidad única: las partidas guardadas (nombre del archivo, fechas, listar, guardar, cargar, borrar).
 * No conoce la vista: devuelve datos o lanza excepciones y el ControladorJuego decide qué mostrar.
 */
public class GestorDePartidasGuardadas {

    private static final DateTimeFormatter FORMATO_VISTA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");

    private Persistencia persistencia = new Persistencia();

    public void setDirectorio(Path directorio) {
        this.persistencia = new Persistencia(directorio);
    }

    /** Guarda la partida y devuelve el nombre con el que quedó guardada. */
    public String guardar(Partida partida) throws IOException {
        String nombre = "Nivel " + partida.getNivelActual().getNumero()
                + (partida.enBatalla() ? " (en batalla)" : " (entre batallas)")
                + " - " + LocalDateTime.now().format(FORMATO_FECHA);
        persistencia.guardar(partida, nombre);
        return nombre;
    }

    public Partida cargar(String nombre) throws IOException, ClassNotFoundException {
        return persistencia.cargar(nombre);
    }

    public void borrar(String nombre) throws IOException {
        persistencia.borrar(nombre);
    }

    /** Las partidas que se pueden cargar. Los archivos dañados o de otra versión no se ofrecen. */
    public List<PartidaGuardadaDTO> listar() {
        List<PartidaGuardadaDTO> guardadas = new ArrayList<>();
        for (String nombre : persistencia.listar()) {
            try {
                Partida p = persistencia.cargar(nombre);
                String fecha = Instant.ofEpochMilli(persistencia.fechaDeGuardado(nombre))
                        .atZone(ZoneId.systemDefault()).format(FORMATO_VISTA);
                guardadas.add(new PartidaGuardadaDTO(nombre, p.getNivelActual().getNumero(),
                        p.enBatalla(), fecha));
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                // Archivo dañado o de otra versión: no se ofrece.
            }
        }
        return guardadas;
    }
}
