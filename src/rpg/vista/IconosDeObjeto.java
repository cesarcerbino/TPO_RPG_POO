package rpg.vista;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;

/** Íconos de 12x12 píxeles para los objetos, dibujados a partir de un mapa de caracteres. */
final class IconosDeObjeto {

    private static final Map<Character, Color> PALETA = Map.of(
            'k', new Color(10, 10, 14),
            'w', Estilo.TEXTO,
            'r', Estilo.PELIGRO,
            's', new Color(150, 144, 160),
            'g', new Color(96, 128, 56),
            'G', new Color(156, 196, 96),
            'v', Estilo.TOXICO,
            'o', new Color(236, 140, 50));

    private static final Map<String, String[]> ICONOS = Map.of(
            "Botiquín", new String[] {
                "............",
                "....kkkk....",
                "....k..k....",
                ".kkkkkkkkkk.",
                ".kwwwwwwwwk.",
                ".kwwwrrwwwk.",
                ".kwwrrrrwwk.",
                ".kwwrrrrwwk.",
                ".kwwwrrwwwk.",
                ".kwwwwwwwwk.",
                ".kkkkkkkkkk.",
                "............"},
            "Suero", new String[] {
                "...kkkkkk...",
                "...kssssk...",
                "...kkkkkk...",
                "....kwwk....",
                "....kwwk....",
                "...kwwwwk...",
                "..kwwwwwwk..",
                "..kvvvvvvk..",
                "..kvwvvvvk..",
                "..kvvvvvvk..",
                "...kkkkkk...",
                "............"},
            "Granada", new String[] {
                "....kkkk....",
                "....kssk.k..",
                "...kkkkkk.k.",
                "..kgGggggk..",
                ".kgGggggggk.",
                ".kgggkkgggk.",
                ".kggggggggk.",
                ".kgggkkgggk.",
                ".kggggggggk.",
                "..kggggggk..",
                "...kkkkkk...",
                "............"},
            "Trampa", new String[] {
                "............",
                ".s.s.ss.s.s.",
                ".ssssssssss.",
                ".s........s.",
                ".s..kkkk..s.",
                ".s..kook..s.",
                ".s..kook..s.",
                ".s..kkkk..s.",
                ".s........s.",
                ".ssssssssss.",
                ".s.s.ss.s.s.",
                "............"},
            // Los equipables se dibujan por ranura (todas las armas comparten ícono, y todas las armaduras).
            "ARMA", new String[] {
                "..........kk",
                ".........kwk",
                "........kwsk",
                ".......kwsk.",
                "......kwsk..",
                ".....kwsk...",
                "..kk.kwsk...",
                "..kkkwsk....",
                "...kook.....",
                "..kookk.....",
                ".kook.......",
                ".kkk........"},
            "ARMADURA", new String[] {
                "............",
                "..kkk..kkk..",
                "..kGk..kGk..",
                ".kgGkkkkGgk.",
                ".kggggggggk.",
                ".kgGsggsGgk.",
                ".kggggggggk.",
                ".kgGsggsGgk.",
                ".kggggggggk.",
                ".kggggggggk.",
                ".kkkkkkkkkk.",
                "............"});

    private IconosDeObjeto() {
    }

    /** Dibuja el ícono del objeto con cada píxel de lado "escala"; si no hay ícono, un signo de pregunta. */
    static void dibujar(Graphics2D g, String nombre, int x, int y, int escala) {
        String[] icono = ICONOS.get(nombre);
        if (icono == null) {
            g.setFont(Estilo.fuente(12f * escala));
            Estilo.textoCentrado(g, "?", x + 6 * escala, y + 10 * escala, Estilo.ORO);
            return;
        }
        for (int fila = 0; fila < icono.length; fila++) {
            for (int col = 0; col < icono[fila].length(); col++) {
                Color c = PALETA.get(icono[fila].charAt(col));
                if (c != null) {
                    g.setColor(c);
                    g.fillRect(x + col * escala, y + fila * escala, escala, escala);
                }
            }
        }
    }
}
