package rpg.persistencia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import rpg.modelo.partida.Partida;


public class Persistencia {


    public static final Path DIRECTORIO_POR_DEFECTO =
            Paths.get(System.getProperty("user.home"), ".rpg-sobrevivientes", "partidas");
    private static final String EXTENSION = ".sav";

    private final Path directorio;

    public Persistencia() {
        this(DIRECTORIO_POR_DEFECTO);
        importarGuardadoAnterior(Paths.get("partida.sav"), "Partida anterior");
        importarGuardadoAnterior(Paths.get("..", "partida.sav"), "Partida anterior (carpeta superior)");
    }

    public Persistencia(Path directorio) {
        this.directorio = directorio;
    }

    public void guardar(Partida partida, String nombre) throws IOException {
        Files.createDirectories(directorio);
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(ruta(nombre)))) {
            out.writeObject(partida);
        }
    }

    public Partida cargar(String nombre) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(ruta(nombre)))) {
            return (Partida) in.readObject();
        }
    }


    public List<String> listar() {
        if (!Files.isDirectory(directorio)) {
            return new ArrayList<>();
        }
        try (Stream<Path> archivos = Files.list(directorio)) {
            return archivos
                    .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(EXTENSION))
                    .sorted(Comparator.comparingLong(Persistencia::modificado).reversed())
                    .map(p -> {
                        String n = p.getFileName().toString();
                        return n.substring(0, n.length() - EXTENSION.length());
                    })
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }


    private void importarGuardadoAnterior(Path viejo, String nombre) {
        try {
            if (Files.isRegularFile(viejo) && !Files.exists(ruta(nombre))) {
                Files.createDirectories(directorio);
                Files.copy(viejo, ruta(nombre));
            }
        } catch (IOException e) {

        }
    }

    public void borrar(String nombre) throws IOException {
        Files.deleteIfExists(ruta(nombre));
    }


    public long fechaDeGuardado(String nombre) {
        return modificado(ruta(nombre));
    }

    private Path ruta(String nombre) {
        return directorio.resolve(nombre + EXTENSION);
    }

    private static long modificado(Path p) {
        try {
            return Files.getLastModifiedTime(p).toMillis();
        } catch (IOException e) {
            return 0;
        }
    }
}
