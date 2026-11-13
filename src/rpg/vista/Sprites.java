package rpg.vista;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import rpg.dto.UnidadDTO;

/**
 * Carga los sprites desde rpg/vista/recursos (el classpath). Si falta alguno devuelve null y la
 * vista dibuja un reemplazo, así el juego sigue funcionando sin las imágenes.
 */
final class Sprites {

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Sprites() {
    }

    static BufferedImage cargar(String nombre) {
        return CACHE.computeIfAbsent(nombre, n -> {
            try (InputStream in = Sprites.class.getResourceAsStream("recursos/" + n + ".png")) {
                return in == null ? null : ImageIO.read(in);
            } catch (IOException e) {
                return null;
            }
        });
    }

    static BufferedImage fondo() {
        return cargar("fondo");
    }

    /**
     * Qué archivo usa cada tipo de unidad. La clave es el identificador estable que manda el modelo
     * (UnidadDTO.claveVisual), NO el nombre de la clase Java: si se renombra una clase, los sprites no se rompen.
     */
    private static final Map<String, String> SPRITE_POR_TIPO = Map.of(
            "BOMBERO", "bombero",
            "MEDICO", "medico",
            "INGENIERO", "ingeniero",
            "MILITAR", "militar",
            "ZOMBIE_COMUN", "comun",
            "ZOMBIE_RAPIDO", "rapido",
            "ZOMBIE_TANQUE", "tanque");

    /** Sprite de una unidad. Los zombies miran a la derecha en el original: se espejan para mirar al equipo. */
    static BufferedImage de(UnidadDTO u) {
        String base = SPRITE_POR_TIPO.get(u.claveVisual());
        if (base == null) {
            return null;   // tipo sin sprite: la vista dibuja un rectángulo de reemplazo
        }
        if (u.esPersonaje()) {
            return cargar(base + "_0");
        }
        BufferedImage img = cargar((u.infectado() ? "infectado_" : "zombie_") + base);
        return img == null ? null : espejo(img);
    }

    private static final Map<BufferedImage, BufferedImage> ESPEJOS = new HashMap<>();

    private static BufferedImage espejo(BufferedImage img) {
        return ESPEJOS.computeIfAbsent(img, i -> {
            BufferedImage r = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = r.createGraphics();
            g.drawImage(i, i.getWidth(), 0, -i.getWidth(), i.getHeight(), null);
            g.dispose();
            return r;
        });
    }
}
