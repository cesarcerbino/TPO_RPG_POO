package rpg.vista;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import rpg.dto.BatallaDTO;
import rpg.dto.UnidadDTO;

/**
 * Escena de combate en pixel art: el equipo a la izquierda, los zombies a la derecha, sobre la
 * autopista en ruinas. Anima respiración, golpes, números flotantes y permite elegir objetivos con el mouse.
 */
class EscenaBatalla extends JPanel {

    private static final int ENEMIGOS = 1000;
    private static final long DURACION_FLOTANTE = 1000;
    private static final long DURACION_GOLPE = 320;
    private static final long DURACION_EMBESTIDA = 420;
    private static final long DURACION_PREPARACION = 600;

    private static final class Flotante {
        final int clave;
        final String texto;
        final Color color;
        final long inicio = System.currentTimeMillis();

        Flotante(int clave, String texto, Color color) {
            this.clave = clave;
            this.texto = texto;
            this.color = color;
        }
    }

    private BatallaDTO actual;
    private final Map<Integer, UnidadDTO> anteriores = new HashMap<>();
    private final Map<Integer, Long> golpes = new HashMap<>();
    private final List<Flotante> flotantes = new ArrayList<>();
    private final Map<Integer, Rectangle> zonas = new HashMap<>();
    private int claveEmbestida = -1;
    private long inicioEmbestida;

    private boolean marcarTurno = true;
    private String cartel;
    private Color colorCartel;

    private boolean eligiendo;
    private boolean objetivosAliados;
    private IntConsumer alElegir;
    private int clavePasada = -1;

    private final Timer reloj = new Timer(33, e -> repaint());

    EscenaBatalla() {
        setPreferredSize(new Dimension(900, 440));
        setMinimumSize(new Dimension(500, 280));
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int clave = claveEn(e.getPoint());
                if (clave != clavePasada) {
                    clavePasada = clave;
                    actualizarCursor();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                clavePasada = -1;
                actualizarCursor();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!eligiendo || !SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                int clave = claveEn(e.getPoint());
                if (esObjetivoValido(clave)) {
                    IntConsumer cb = alElegir;
                    terminarEleccion();
                    cb.accept(clave >= ENEMIGOS ? clave - ENEMIGOS : clave);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        reloj.start();
    }

    @Override
    public void removeNotify() {
        reloj.stop();
        super.removeNotify();
    }

    // ---------------------------------------------------------------- estado

    void reiniciar() {
        anteriores.clear();
        golpes.clear();
        flotantes.clear();
        claveEmbestida = -1;
        actual = null;
        cartel = null;
        terminarEleccion();
    }

    void mostrar(BatallaDTO nueva) {
        mostrar(nueva, true, null, null);
    }

    /**
     * Muestra un estado nuevo: el que actuó en el estado anterior embiste y se ven los cambios de vida.
     * marcarTurno indica si se señala a la unidad en turno; cartel (puede ser null) es un aviso arriba de la escena.
     */
    void mostrar(BatallaDTO nueva, boolean marcarTurno, String cartel, Color colorCartel) {
        this.marcarTurno = marcarTurno;
        this.cartel = cartel;
        this.colorCartel = colorCartel;
        long ahora = System.currentTimeMillis();
        if (actual != null && actual.unidadEnTurno() != null) {
            claveEmbestida = clave(actual.unidadEnTurno());
            inicioEmbestida = ahora;
        }
        registrarCambios(nueva.equipo(), ahora);
        registrarCambios(nueva.enemigos(), ahora);
        actual = nueva;
        terminarEleccion();
        repaint();
    }

    private void registrarCambios(List<UnidadDTO> lista, long ahora) {
        for (UnidadDTO u : lista) {
            int c = clave(u);
            UnidadDTO antes = anteriores.get(c);
            if (antes != null) {
                int delta = u.vida() - antes.vida();
                if (delta < 0) {
                    golpes.put(c, ahora);
                    flotantes.add(new Flotante(c, String.valueOf(delta), Estilo.PELIGRO));
                } else if (delta > 0) {
                    flotantes.add(new Flotante(c, "+" + delta, Estilo.VIDA));
                }
                if (u.infectado() && !antes.infectado()) {
                    flotantes.add(new Flotante(c, "INFECTADO", Estilo.INFECTADO));
                }
            }
            anteriores.put(c, u);
        }
    }

    private static int clave(UnidadDTO u) {
        return (u.esPersonaje() ? 0 : ENEMIGOS) + u.indice();
    }

    // ---------------------------------------------------------------- elección de objetivo

    /** Resalta a los vivos del bando pedido; el próximo clic sobre uno llama a alElegir con su índice. */
    void pedirObjetivo(boolean aliados, IntConsumer alElegir) {
        this.eligiendo = true;
        this.objetivosAliados = aliados;
        this.alElegir = alElegir;
        actualizarCursor();
        repaint();
    }

    void cancelarEleccion() {
        terminarEleccion();
        repaint();
    }

    private void terminarEleccion() {
        eligiendo = false;
        alElegir = null;
        actualizarCursor();
    }

    private boolean esObjetivoValido(int clave) {
        if (!eligiendo || actual == null || clave < 0) {
            return false;
        }
        boolean esEnemigo = clave >= ENEMIGOS;
        if (esEnemigo == objetivosAliados) {
            return false;
        }
        List<UnidadDTO> lista = esEnemigo ? actual.enemigos() : actual.equipo();
        int indice = esEnemigo ? clave - ENEMIGOS : clave;
        for (UnidadDTO u : lista) {
            if (u.indice() == indice) {
                return u.vivo();
            }
        }
        return false;
    }

    private void actualizarCursor() {
        setCursor(Cursor.getPredefinedCursor(esObjetivoValido(clavePasada) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private int claveEn(java.awt.Point p) {
        int mejor = -1;
        for (Map.Entry<Integer, Rectangle> e : zonas.entrySet()) {
            if (e.getValue().contains(p)) {
                mejor = e.getKey();
            }
        }
        return mejor;
    }

    // ---------------------------------------------------------------- dibujo

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Estilo.calidad(g);
        int w = getWidth();
        int h = getHeight();
        PanelConFondo.dibujarFondo(g, w, h);
        g.setColor(new Color(10, 8, 14, 50));
        g.fillRect(0, 0, w, h);

        zonas.clear();
        if (actual != null) {
            long ahora = System.currentTimeMillis();
            List<UnidadDTO> todos = new ArrayList<>(actual.equipo());
            todos.addAll(actual.enemigos());
            // los más "atrás" (baseline menor) primero para que el solapamiento se vea natural
            todos.sort((a, b) -> Integer.compare(baseline(a, h), baseline(b, h)));
            for (UnidadDTO u : todos) {
                dibujarUnidad(g, u, w, h, ahora);
            }
            dibujarFlotantes(g, ahora);
            dibujarInfo(g, w);
            if (eligiendo) {
                dibujarCartel(g, objetivosAliados ? "ELEGÍ UN ALIADO" : "ELEGÍ UN ENEMIGO", Estilo.ORO, w);
            } else if (cartel != null) {
                dibujarCartel(g, cartel, colorCartel, w);
            }
        }
        g.dispose();
    }

    private static void dibujarCartel(Graphics2D g, String msg, Color color, int w) {
        g.setFont(Estilo.fuente(16f));
        int tw = g.getFontMetrics().stringWidth(msg) + 40;
        Estilo.caja(g, (w - tw) / 2, 12, tw, 34, Estilo.PANEL, color);
        Estilo.textoCentrado(g, msg, w / 2, 35, color);
    }

    private double factor(int h) {
        return (h * 0.42) / 300.0;
    }

    private int baseline(UnidadDTO u, int h) {
        List<UnidadDTO> lista = u.esPersonaje() ? actual.equipo() : actual.enemigos();
        int i = lista.indexOf(u);
        int base = (int) (h * 0.84);
        return base - (i % 2 == 1 ? (int) (h * 0.07) : 0);
    }

    private Rectangle rectangulo(UnidadDTO u, int w, int h, BufferedImage sprite) {
        List<UnidadDTO> lista = u.esPersonaje() ? actual.equipo() : actual.enemigos();
        int i = lista.indexOf(u);
        int n = Math.max(1, lista.size());
        double f = factor(h);
        int sw = sprite == null ? (int) (130 * f) : (int) (sprite.getWidth() * f);
        int sh = sprite == null ? (int) (280 * f) : (int) (sprite.getHeight() * f);
        // centros repartidos: equipo en el 10%-38% del ancho, zombies en el 62%-90%
        double cx;
        if (n == 1) {
            cx = u.esPersonaje() ? w * 0.22 : w * 0.76;
        } else if (u.esPersonaje()) {
            cx = w * (0.10 + 0.28 * i / (n - 1));
        } else {
            cx = w * (0.90 - 0.28 * (n - 1 - i) / (n - 1));
        }
        int base = baseline(u, h);
        return new Rectangle((int) cx - sw / 2, base - sh, sw, sh);
    }

    private void dibujarUnidad(Graphics2D g, UnidadDTO u, int w, int h, long ahora) {
        BufferedImage sprite = Sprites.de(u);
        Rectangle r = rectangulo(u, w, h, sprite);
        int c = clave(u);
        int base = r.y + r.height;
        int cx = r.x + r.width / 2;

        UnidadDTO enTurno = actual.unidadEnTurno();
        boolean esElDelTurno = marcarTurno && enTurno != null && !eligiendo && u.vivo() && clave(enTurno) == c;

        // un enemigo que está por actuar se echa hacia atrás y tiembla, como tomando envión
        if (esElDelTurno && !u.esPersonaje()) {
            double avance = Math.min(1.0, (ahora - inicioEmbestida) / (double) DURACION_PREPARACION);
            r.x += (int) (avance * w * 0.025 + Math.sin(ahora * 0.06) * 2 * avance);
            cx = r.x + r.width / 2;
        }
        // embestida del último en actuar
        if (c == claveEmbestida) {
            long t = ahora - inicioEmbestida;
            if (t < DURACION_EMBESTIDA) {
                double dir = u.esPersonaje() ? 1 : -1;
                r.x += (int) (Math.sin(Math.PI * t / DURACION_EMBESTIDA) * dir * w * 0.07);
                cx = r.x + r.width / 2;
            }
        }
        // temblor al recibir daño
        Long golpe = golpes.get(c);
        boolean golpeado = golpe != null && ahora - golpe < DURACION_GOLPE;
        if (golpeado) {
            r.x += (int) (Math.sin((ahora - golpe) * 0.12) * 6);
            cx = r.x + r.width / 2;
        }

        zonas.put(c, new Rectangle(r.x - 8, r.y - 34, r.width + 16, r.height + 60));

        // sombra
        g.setColor(new Color(0, 0, 0, u.vivo() ? 110 : 70));
        g.fillOval(cx - r.width / 2, base - 9, r.width, 18);

        // respiración leve (solo vivos)
        double resp = u.vivo() ? 1.0 + 0.012 * Math.sin(ahora * 0.004 + c * 1.7) : 1.0;
        int dh = (int) (r.height * resp);

        java.awt.Graphics2D gs = (Graphics2D) g.create();
        if (!u.vivo()) {
            gs.translate(u.esPersonaje() ? dh / 2 : -dh / 2, 0);
            gs.rotate(Math.toRadians(u.esPersonaje() ? -88 : 88), cx, base);
            gs.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        }
        if (sprite != null) {
            BufferedImage dibujo = sprite;
            if (golpeado) {
                dibujo = tenir(sprite, new Color(255, 40, 40, 150));
            } else if (u.infectado() && u.esPersonaje()) {
                dibujo = tenir(sprite, new Color(176, 90, 230, 80));
            }
            gs.drawImage(dibujo, r.x, base - dh, r.width, dh, null);
        } else {
            gs.setColor(u.esPersonaje() ? new Color(70, 130, 90) : new Color(130, 60, 60));
            gs.fillRect(r.x, base - dh, r.width, dh);
        }
        gs.dispose();

        if (u.vivo()) {
            dibujarBarras(g, u, cx, r.y);
            dibujarNombre(g, u, cx, base);
        } else {
            g.setFont(Estilo.fuente(12f));
            Estilo.textoCentrado(g, "K.O.", cx, base + 22, Estilo.TEXTO_TENUE);
        }

        if (esElDelTurno) {
            Color col = u.esPersonaje() ? Estilo.ORO : Estilo.PELIGRO;
            if (!u.esPersonaje()) {
                Color pulso = new Color(col.getRed(), col.getGreen(), col.getBlue(), 150 + (int) (90 * Math.sin(ahora * 0.012)));
                dibujarEsquinas(g, new Rectangle(r.x - 6, r.y - 6, r.width + 12, r.height + 12), pulso, 3);
            }
            dibujarFlecha(g, cx, r.y - 66 - (int) (4 * Math.sin(ahora * 0.008)), col);
        }
        if (eligiendo && esObjetivoValido(c)) {
            boolean encima = c == clavePasada;
            Color col = encima ? Estilo.ORO : new Color(Estilo.TOXICO.getRed(), Estilo.TOXICO.getGreen(), Estilo.TOXICO.getBlue(),
                    150 + (int) (80 * Math.sin(ahora * 0.008)));
            dibujarEsquinas(g, new Rectangle(r.x - 6, r.y - 6, r.width + 12, r.height + 12), col, encima ? 3 : 2);
            dibujarFlecha(g, cx, r.y - 66 - (int) (4 * Math.sin(ahora * 0.01)), col);
        }
    }

    private void dibujarBarras(Graphics2D g, UnidadDTO u, int cx, int yCabeza) {
        int ancho = 96;
        int x = cx - ancho / 2;
        int y = yCabeza - 20;
        double fv = u.vidaMaxima() == 0 ? 0 : u.vida() / (double) u.vidaMaxima();
        Color cv = fv > 0.5 ? (u.esPersonaje() ? Estilo.VIDA : Estilo.VIDA_ENEMIGO)
                : fv > 0.25 ? Estilo.ORO : Estilo.PELIGRO;
        Estilo.barra(g, x, y, ancho, 9, fv, cv, Estilo.VIDA_FONDO);
        if (u.esPersonaje()) {
            double fm = u.manaMaximo() == 0 ? 0 : u.mana() / (double) u.manaMaximo();
            Estilo.barra(g, x, y + 12, ancho, 5, fm, Estilo.MANA, new Color(14, 22, 48));
        }
        g.setFont(Estilo.fuente(11f));
        Estilo.textoCentrado(g, u.vida() + "/" + u.vidaMaxima(), cx, y - 4, Estilo.TEXTO);
    }

    private void dibujarNombre(Graphics2D g, UnidadDTO u, int cx, int base) {
        g.setFont(Estilo.fuente(12f));
        String nombre = u.esPersonaje() ? u.nombre() + " Nv." + u.nivel() : u.nombre();
        Estilo.textoCentrado(g, nombre, cx, base + 24, Estilo.TEXTO);
        StringBuilder estados = new StringBuilder();
        if (u.infectado()) {
            estados.append("INFECTADO ");
        }
        if (u.turnosBloqueado() > 0) {
            estados.append("BLOQUEADO(").append(u.turnosBloqueado()).append(") ");
        }
        if (u.defendiendo()) {
            estados.append("DEFENDIENDO");
        }
        if (estados.length() > 0) {
            g.setFont(Estilo.fuente(10f));
            Estilo.textoCentrado(g, estados.toString().trim(), cx, base + 37, u.infectado() ? Estilo.INFECTADO : Estilo.MANA);
        }
    }

    private static void dibujarFlecha(Graphics2D g, int cx, int y, Color c) {
        Polygon p = new Polygon(new int[] {cx - 10, cx + 10, cx}, new int[] {y, y, y + 14}, 3);
        g.setColor(Color.BLACK);
        g.fillPolygon(new Polygon(new int[] {cx - 13, cx + 13, cx}, new int[] {y - 3, y - 3, y + 19}, 3));
        g.setColor(c);
        g.fillPolygon(p);
    }

    private static void dibujarEsquinas(Graphics2D g, Rectangle r, Color c, int grosor) {
        int l = 18;
        g.setColor(c);
        int[][] esquinas = {{r.x, r.y, 1, 1}, {r.x + r.width, r.y, -1, 1},
                {r.x, r.y + r.height, 1, -1}, {r.x + r.width, r.y + r.height, -1, -1}};
        for (int[] e : esquinas) {
            g.fillRect(Math.min(e[0], e[0] + e[2] * l), e[1] - (e[3] < 0 ? grosor : 0), l, grosor);
            g.fillRect(e[0] - (e[2] < 0 ? grosor : 0), Math.min(e[1], e[1] + e[3] * l), grosor, l);
        }
    }

    private void dibujarFlotantes(Graphics2D g, long ahora) {
        g.setFont(Estilo.fuente(24f));
        Iterator<Flotante> it = flotantes.iterator();
        while (it.hasNext()) {
            Flotante f = it.next();
            long t = ahora - f.inicio;
            if (t > DURACION_FLOTANTE) {
                it.remove();
                continue;
            }
            Rectangle z = zonas.get(f.clave);
            if (z == null) {
                continue;
            }
            float alfa = t < 600 ? 1f : 1f - (t - 600) / 400f;
            int y = z.y + 20 - (int) (t * 0.06);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alfa)));
            boolean largo = f.texto.length() > 6;
            g.setFont(Estilo.fuente(largo ? 14f : 26f));
            Estilo.textoCentrado(g, f.texto, z.x + z.width / 2, y, f.color);
            g.setComposite(AlphaComposite.SrcOver);
        }
    }

    /** Cuadro con las estadísticas de la unidad bajo el mouse. */
    private void dibujarInfo(Graphics2D g, int w) {
        if (clavePasada < 0 || actual == null) {
            return;
        }
        UnidadDTO u = buscar(clavePasada);
        Rectangle z = zonas.get(clavePasada);
        if (u == null || z == null) {
            return;
        }
        List<String> lineas = new ArrayList<>();
        lineas.add(u.nombre() + (u.esPersonaje() ? "  (" + u.tipo() + " Nv." + u.nivel() + ")" : ""));
        lineas.add("ATK " + u.ataque() + "   DEF " + u.defensa() + "   VEL " + u.velocidad());
        if (u.esPersonaje()) {
            lineas.add("EXP " + u.experiencia() + "/" + u.experienciaParaSubir());
        }
        g.setFont(Estilo.fuente(12f));
        int ancho = 0;
        for (String s : lineas) {
            ancho = Math.max(ancho, g.getFontMetrics().stringWidth(s));
        }
        ancho += 28;
        int alto = lineas.size() * 17 + 20;
        int x = Math.max(6, Math.min(w - ancho - 6, z.x + z.width / 2 - ancho / 2));
        int y = Math.max(6, z.y - alto - 44);
        Estilo.caja(g, x, y, ancho, alto, Estilo.PANEL, Estilo.BORDE);
        for (int i = 0; i < lineas.size(); i++) {
            Estilo.texto(g, lineas.get(i), x + 14, y + 24 + i * 17, i == 0 ? Estilo.ORO : Estilo.TEXTO);
        }
    }

    private UnidadDTO buscar(int clave) {
        boolean enemigo = clave >= ENEMIGOS;
        int indice = enemigo ? clave - ENEMIGOS : clave;
        for (UnidadDTO u : enemigo ? actual.enemigos() : actual.equipo()) {
            if (u.indice() == indice) {
                return u;
            }
        }
        return null;
    }

    private static BufferedImage tenir(BufferedImage src, Color tinte) {
        BufferedImage r = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = r.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(tinte);
        g.fillRect(0, 0, r.getWidth(), r.getHeight());
        g.dispose();
        return r;
    }
}
