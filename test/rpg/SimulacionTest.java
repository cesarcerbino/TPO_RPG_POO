package rpg;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rpg.controlador.ControladorJuego;
import rpg.dto.BatallaDTO;
import rpg.dto.EquipableDTO;
import rpg.dto.EquipamientoDTO;
import rpg.dto.HabilidadDTO;
import rpg.dto.ObjetoDTO;
import rpg.dto.PartidaDTO;
import rpg.dto.ResultadoBatallaDTO;
import rpg.dto.UnidadDTO;
import rpg.modelo.accion.AccionInvalidaException;
import rpg.modelo.accion.AtacarAccion;
import rpg.modelo.accion.DefenderAccion;
import rpg.modelo.batalla.Batalla;
import rpg.modelo.equipamiento.Equipable;
import rpg.modelo.equipamiento.Ranura;
import rpg.modelo.generacion.FabricaDeEquipables;
import rpg.modelo.generacion.GeneradorAleatorio;
import rpg.modelo.objeto.Inventario;
import rpg.modelo.objeto.Suero;
import rpg.modelo.partida.Equipo;
import rpg.modelo.unidad.Bando;
import rpg.modelo.unidad.Bombero;
import rpg.modelo.unidad.Enemigo;
import rpg.modelo.unidad.Medico;
import rpg.modelo.unidad.Militar;
import rpg.modelo.unidad.Personaje;
import rpg.modelo.unidad.Unidad;
import rpg.modelo.unidad.ZombieComun;
import rpg.modelo.unidad.ZombieRapido;
import rpg.modelo.unidad.ZombieTanque;
import rpg.controlador.IVista;

/**
 * Pruebas sin interfaz gráfica. Se corre con test.sh / test.bat.
 *  1) Simula muchas partidas completas con un jugador automático.
 *  2) Guardar y cargar una partida.
 *  3) Orden de turnos intercalado entre bandos.
 *  4) Contagio al morir un infectado.
 *  5) Subida de nivel.
 *  6) Infectado pierde el turno.
 *  7) El Modelo rechaza acciones inválidas sin consumir el turno.
 *  8) Infección: bloqueo limitado y el suero lo limpia.
 *  9) Equipamiento: bonus, cambio de pieza, mochila, botín por nivel y solo entre batallas.
 */
public class SimulacionTest {

    /** Vista falsa: guarda lo último que le pidió mostrar el controlador. */
    static class VistaFalsa implements IVista {
        BatallaDTO batalla;
        ResultadoBatallaDTO resultado;
        PartidaDTO partida;
        EquipamientoDTO equipamiento;
        String mensaje;
        int eventos;

        public void mostrarMenuPrincipal() { }
        public void mostrarSeleccionPersonajes(List<UnidadDTO> d, int min, int max) { }
        public void mostrarBatalla(BatallaDTO b) { batalla = b; }
        public void mostrarTurnoEnemigo(BatallaDTO b) { }
        public void mostrarFinDeBatalla(BatallaDTO b) { }
        public void mostrarResultado(ResultadoBatallaDTO r, PartidaDTO p) { resultado = r; partida = p; }
        public void mostrarEquipamiento(EquipamientoDTO e) { equipamiento = e; }
        public void mostrarEvento(String t) { eventos++; }
        public void limpiarRegistro() { }
        public void mostrarMensaje(String t) { mensaje = t; }
        List<String> guardadas;
        public void mostrarPartidasGuardadas(List<rpg.dto.PartidaGuardadaDTO> n) {
            guardadas = n.stream().map(rpg.dto.PartidaGuardadaDTO::nombre).toList();
        }
    }

    public static void main(String[] args) throws Exception {
        simularPartidas(300, List.of(0, 2, 4, 5));   // equipo de 4
        simularPartidas(300, List.of(0, 5, 2));      // equipo de 3
        simularPartidas(300, List.of(0, 5));         // equipo de 2
        guardarYCargar();
        ordenDeTurnosIntercalado();
        contagioAlMorir();
        subidaDeNivel();
        infectadoPierdeElTurno();
        modeloRechazaAccionesInvalidas();
        infeccionSoloBloqueaAlInfectarse();
        equipamiento();
        System.out.println("TODAS LAS PRUEBAS OK");
    }

    // ---------------------------------------------------------------- 1) partidas completas

    static void simularPartidas(int partidas, List<Integer> equipoElegido) {
        // Los niveles son infinitos: toda partida termina con el equipo derrotado; se mide hasta dónde llega.
        int sumaNiveles = 0;
        int nivelMinimo = Integer.MAX_VALUE;
        int nivelMaximo = 0;
        for (int seed = 0; seed < partidas; seed++) {
            VistaFalsa vista = new VistaFalsa();
            ControladorJuego c = new ControladorJuego(vista, new GeneradorAleatorio(seed));
            Random azar = new Random(seed);
            c.iniciar();
            c.onNuevaPartida();
            c.onEquipoElegido(equipoElegido);

            for (int guarda = 0; ; guarda++) {
                if (guarda > 200_000) {
                    throw new IllegalStateException("La partida " + seed + " no termina (bucle infinito)");
                }
                if (vista.resultado != null) {
                    if (vista.partida.terminada()) {
                        if (vista.resultado.victoria()) {
                            throw new IllegalStateException("La partida " + seed + " terminó sin que cayera el equipo");
                        }
                        int nivel = vista.partida.nivel();
                        sumaNiveles += nivel;
                        nivelMinimo = Math.min(nivelMinimo, nivel);
                        nivelMaximo = Math.max(nivelMaximo, nivel);
                        break;
                    }
                    vista.resultado = null;
                    equiparLoMejor(c, vista);
                    c.onContinuar();
                    continue;
                }
                BatallaDTO b = vista.batalla;
                vista.batalla = null;
                if (b == null) {
                    throw new IllegalStateException("La vista no recibió nada (partida " + seed + ")");
                }
                jugar(c, b, azar);
            }
            if (vista.eventos == 0) {
                throw new IllegalStateException("El observador no recibió eventos");
            }
        }
        System.out.println("[1] Equipo de " + equipoElegido.size() + ": " + partidas + " partidas sin errores. Nivel alcanzado: promedio "
                + String.format("%.1f", sumaNiveles / (double) partidas) + " | mínimo " + nivelMinimo
                + " | máximo " + nivelMaximo);
    }

    /**
     * Entre batallas, el jugador automático abre el equipamiento y se pone cada pieza de la mochila que mejore
     * lo que alguien tiene en esa ranura. Cada cambio sube el total equipado, así que el bucle termina.
     */
    private static void equiparLoMejor(ControladorJuego c, VistaFalsa vista) {
        vista.equipamiento = null;
        c.onAbrirEquipamiento();
        if (vista.equipamiento == null) {
            throw new IllegalStateException("Entre batallas debería poder abrirse el equipamiento");
        }
        boolean cambio = true;
        while (cambio) {
            cambio = false;
            for (EquipableDTO e : vista.equipamiento.mochila()) {
                for (UnidadDTO u : vista.equipamiento.equipo()) {
                    if (!cambio && u.vivo() && valor(e) > valor(puesto(u, e.ranura()))) {
                        c.onEquipar(u.indice(), e.indice());
                        cambio = true;
                    }
                }
                if (cambio) {
                    break;
                }
            }
        }
    }

    private static int valor(EquipableDTO e) {
        return e == null ? 0 : e.bonusAtaque() + e.bonusDefensa();
    }

    private static EquipableDTO puesto(UnidadDTO u, String ranura) {
        return u.equipados().stream().filter(e -> e.ranura().equals(ranura)).findFirst().orElse(null);
    }

    /** Jugador automático: mezcla ataques, habilidades, objetos y defensa para ejercitar todos los caminos. */
    private static void jugar(ControladorJuego c, BatallaDTO b, Random azar) {
        UnidadDTO yo = b.unidadEnTurno();
        List<UnidadDTO> enemigos = b.enemigos().stream().filter(UnidadDTO::vivo).toList();
        List<UnidadDTO> aliados = b.equipo().stream().filter(UnidadDTO::vivo).toList();
        int dado = azar.nextInt(10);

        if (dado == 0) {
            c.onDefender();
        } else if (dado <= 2 && !b.objetos().isEmpty()) {
            ObjetoDTO o = b.objetos().get(azar.nextInt(b.objetos().size()));
            int t = -1;
            if (o.requiereObjetivo()) {
                List<UnidadDTO> l = o.paraAliados() ? aliados : enemigos;
                t = l.get(azar.nextInt(l.size())).indice();
            }
            c.onUsarObjeto(o.indice(), t);
        } else if (dado <= 6 && !yo.habilidades().isEmpty() && yo.mana() >= yo.habilidades().get(0).costoMana()) {
            HabilidadDTO h = yo.habilidades().get(0);
            int t = -1;
            if (h.requiereObjetivo()) {
                List<UnidadDTO> l = h.paraAliados() ? aliados : enemigos;
                t = l.get(azar.nextInt(l.size())).indice();
            }
            c.onUsarHabilidad(h.indice(), t);
        } else {
            c.onAtacar(enemigos.get(azar.nextInt(enemigos.size())).indice());
        }
    }

    // ---------------------------------------------------------------- 2) guardar / cargar

    static void guardarYCargar() throws Exception {
        Path dir = Files.createTempDirectory("rpg-test");
        try {
            VistaFalsa v1 = new VistaFalsa();
            ControladorJuego c1 = new ControladorJuego(v1, new GeneradorAleatorio(7));
            c1.setDirectorioDePartidas(dir);
            c1.onNuevaPartida();
            c1.onEquipoElegido(List.of(0, 2));

            // Guardado en plena batalla: se retoma la misma batalla, no una nueva.
            int turnoGuardado = v1.batalla.turno();
            String enTurno = v1.batalla.unidadEnTurno().nombre();
            c1.onGuardar();
            Thread.sleep(1100);   // otro nombre (la fecha llega al segundo)

            // Jugar hasta terminar la primera batalla.
            Random azar = new Random(7);
            while (v1.resultado == null) {
                BatallaDTO b = v1.batalla;
                v1.batalla = null;
                jugar(c1, b, azar);
            }
            if (v1.partida.terminada()) {
                throw new IllegalStateException("La prueba de guardado necesita una partida en curso");
            }
            if (!v1.partida.puedeGuardar()) {
                throw new IllegalStateException("Entre batallas debería poder guardarse");
            }
            c1.onGuardarYVolverAlMenu();

            VistaFalsa v2 = new VistaFalsa();
            ControladorJuego c2 = new ControladorJuego(v2, new GeneradorAleatorio(99));
            c2.setDirectorioDePartidas(dir);
            c2.onCargarPartida();
            if (v2.guardadas == null || v2.guardadas.size() != 2) {
                throw new IllegalStateException("Debería haber 2 partidas guardadas: " + v2.guardadas);
            }
            // La más reciente (entre batallas) arranca la siguiente batalla.
            c2.onCargarPartida(v2.guardadas.get(0));
            if (v2.batalla == null || v2.batalla.equipo().size() != 2) {
                throw new IllegalStateException("Al cargar debería empezar la siguiente batalla. Mensaje: " + v2.mensaje);
            }
            // La otra (en batalla) retoma exactamente el mismo turno.
            VistaFalsa v3 = new VistaFalsa();
            ControladorJuego c3 = new ControladorJuego(v3, new GeneradorAleatorio(5));
            c3.setDirectorioDePartidas(dir);
            c3.onCargarPartida(v2.guardadas.get(1));
            if (v3.batalla == null || v3.batalla.turno() != turnoGuardado
                    || !v3.batalla.unidadEnTurno().nombre().equals(enTurno)) {
                throw new IllegalStateException("La batalla guardada no se retomó en el mismo punto");
            }
            // Borrar una partida: queda solo la otra.
            c3.onBorrarPartida(v2.guardadas.get(1));
            VistaFalsa v4 = new VistaFalsa();
            ControladorJuego c4 = new ControladorJuego(v4, new GeneradorAleatorio(5));
            c4.setDirectorioDePartidas(dir);
            c4.onCargarPartida();
            if (v4.guardadas == null || v4.guardadas.size() != 1 || !v4.guardadas.get(0).equals(v2.guardadas.get(0))) {
                throw new IllegalStateException("El borrado no dejó solo la otra partida: " + v4.guardadas);
            }
            System.out.println("[2] Guardar, cargar y borrar OK");
        } finally {
            try (var archivos = Files.list(dir)) {
                for (Path f : archivos.toList()) {
                    Files.deleteIfExists(f);
                }
            }
            Files.deleteIfExists(dir);
        }
    }

    // ---------------------------------------------------------------- infección bloquea el turno

    static void infectadoPierdeElTurno() {
        GeneradorAleatorio rng = new GeneradorAleatorio(11);
        Bombero a = new Bombero("A");
        Medico m = new Medico("M");
        Equipo e = new Equipo(new ArrayList<>(List.of(a, m)), new Inventario());
        Batalla b = new Batalla(e, new ArrayList<>(List.of(new ZombieComun(1, rng))), rng);
        a.infectar();
        boolean actuo = false;
        for (int i = 0; i < 8; i++) {
            Unidad u = b.unidadEnTurno();
            if (u == a) {
                actuo = true;
                break;
            }
            b.ejecutarTurno(new DefenderAccion(u));
        }
        boolean salteo = b.getRegistro().getHistorial().stream()
                .anyMatch(l -> l.equals("A está infectado y pierde el turno."));
        if (!salteo || (actuo && a.getTurnosSalteados() != 0)) {
            throw new IllegalStateException("El infectado no perdió el turno");
        }
        System.out.println("[6] Infectado pierde el turno OK");
    }

    // ---------------------------------------------------------------- 7) validación en el Modelo

    static void modeloRechazaAccionesInvalidas() {
        GeneradorAleatorio rng = new GeneradorAleatorio(5);
        Bombero a = new Bombero("A");
        Medico m = new Medico("M");
        Equipo e = new Equipo(new ArrayList<>(List.of(a, m)), new Inventario());
        Enemigo z1 = new ZombieComun(1, rng);
        Enemigo z2 = new ZombieComun(1, rng);
        Batalla b = new Batalla(e, new ArrayList<>(List.of(z1, z2)), rng);
        while (b.unidadEnTurno().getBando() != Bando.JUGADOR) {
            b.jugarTurnoAutomatico();
        }
        Unidad actor = b.unidadEnTurno();
        int turno = b.getTurnoNumero();
        z1.recibirDano(z1.getVida());   // z1 cae "por fuera"

        esperarRechazo(b, new AtacarAccion(z1), "atacar a un enemigo caído");
        esperarRechazo(b, new AtacarAccion(m), "atacar a un aliado");
        esperarRechazo(b, new AtacarAccion(null), "atacar sin objetivo");
        if (b.unidadEnTurno() != actor || b.getTurnoNumero() != turno) {
            throw new IllegalStateException("Una acción inválida consumió el turno");
        }
        b.ejecutarTurno(new AtacarAccion(z2));   // la válida sí se ejecuta
        if (b.getTurnoNumero() != turno + 1) {
            throw new IllegalStateException("La acción válida no se ejecutó");
        }
        System.out.println("[7] El Modelo rechaza acciones inválidas sin consumir el turno OK");
    }

    private static void esperarRechazo(Batalla b, rpg.modelo.accion.Accion a, String caso) {
        try {
            b.ejecutarTurno(a);
        } catch (AccionInvalidaException ok) {
            return;
        }
        throw new IllegalStateException("El Modelo permitió " + caso);
    }

    // ---------------------------------------------------------------- 8) infección: bloqueo limitado

    static void infeccionSoloBloqueaAlInfectarse() {
        Bombero a = new Bombero("A");
        a.infectar();
        if (a.getTurnosSalteados() != Unidad.TURNOS_BLOQUEO_INFECCION) {
            throw new IllegalStateException("Al infectarse debería quedar bloqueado");
        }
        for (int i = 0; i < Unidad.TURNOS_BLOQUEO_INFECCION; i++) {
            a.consumirTurnoSalteado();
        }
        if (a.estaBloqueado() || !a.isInfectado()) {
            throw new IllegalStateException("Pasado el bloqueo debería poder actuar, pero seguir infectado");
        }
        Medico m = new Medico("M");
        m.infectar();
        GeneradorAleatorio rng = new GeneradorAleatorio(2);
        Inventario inv = new Inventario();
        Suero suero = new Suero(rng, 1);
        inv.agregar(suero);
        Batalla b = new Batalla(new Equipo(new ArrayList<>(List.of(a, m)), inv),
                new ArrayList<>(List.of(new ZombieComun(1, rng))), rng);
        suero.usar(a, m, b);
        if (m.isInfectado() || m.estaBloqueado()) {
            throw new IllegalStateException("El suero debería quitar la infección y el bloqueo");
        }
        System.out.println("[8] Infección: bloqueo limitado y el suero lo limpia OK");
    }

    private static List<String> nombres(List<String> l) {
        return new ArrayList<>(l);
    }

    // ---------------------------------------------------------------- 3) orden de turnos

    static void ordenDeTurnosIntercalado() {
        GeneradorAleatorio rng = new GeneradorAleatorio(1);
        List<Personaje> equipo = new ArrayList<>(List.of(new Bombero("A"), new Militar("B"), new Medico("C")));
        Equipo e = new Equipo(equipo, new Inventario());
        List<Enemigo> enemigos = new ArrayList<>(List.of(new ZombieComun(1, rng), new ZombieRapido(1, rng), new ZombieTanque(1, rng)));
        Batalla b = new Batalla(e, enemigos, rng);

        // Todos solo se defienden: nadie muere, así que los bandos deben alternar siempre.
        Boolean anterior = null;
        for (int i = 0; i < 30; i++) {
            Unidad u = b.unidadEnTurno();
            boolean esEquipo = u.getBando() == Bando.JUGADOR;
            if (anterior != null && anterior == esEquipo) {
                throw new IllegalStateException("Un bando actuó dos veces seguidas en el turno " + i);
            }
            anterior = esEquipo;
            b.ejecutarTurno(new DefenderAccion(u));
        }
        System.out.println("[3] Orden de turnos intercalado OK");
    }

    // ---------------------------------------------------------------- 4) contagio

    static void contagioAlMorir() {
        GeneradorAleatorio rng = new GeneradorAleatorio(3);
        List<Personaje> equipo = new ArrayList<>(List.of(new Bombero("A"), new Medico("B")));
        Equipo e = new Equipo(equipo, new Inventario());
        Enemigo infectado = new ZombieComun(1, rng);
        Enemigo otro = new ZombieComun(1, rng);
        infectado.infectar();
        infectado.recibirDano(infectado.getVida() - 1);   // le queda 1 de vida
        Batalla b = new Batalla(e, new ArrayList<>(List.of(infectado, otro)), rng);

        for (int i = 0; i < 20 && infectado.estaVivo(); i++) {
            Unidad u = b.unidadEnTurno();
            if (u.getBando() == Bando.JUGADOR) {
                b.ejecutarTurno(new AtacarAccion(infectado));
            } else {
                b.ejecutarTurno(new DefenderAccion(u));
            }
        }
        if (infectado.estaVivo()) {
            throw new IllegalStateException("El zombie infectado no murió");
        }
        boolean alguienInfectado = otro.isInfectado() || equipo.stream().anyMatch(Unidad::isInfectado);
        if (!alguienInfectado) {
            throw new IllegalStateException("La infección no se transmitió al morir");
        }
        System.out.println("[4] Contagio al morir un infectado OK");
    }

    // ---------------------------------------------------------------- 5) niveles

    static void subidaDeNivel() {
        Bombero p = new Bombero("Rex");
        int vidaAntes = p.getVidaMaxima();
        int costoAntes = p.getHabilidades().get(0).getCostoMana();
        p.ganarExperiencia(p.experienciaParaSubir());
        if (p.getNivel() != 2 || p.getVidaMaxima() <= vidaAntes) {
            throw new IllegalStateException("No subió de nivel como corresponde");
        }
        if (p.getHabilidades().get(0).getCostoMana() <= costoAntes) {
            throw new IllegalStateException("El costo de la habilidad debería subir con el nivel");
        }
        System.out.println("[5] Subida de nivel OK");
    }
    static void equipamiento() {
        Bombero p = new Bombero("Rex");
        Bombero sinNada = new Bombero("Maya");
        Equipable manopla = new Equipable("Manopla", "Puño reforzado con metal", Ranura.ARMA, 2, 0);
        Equipable motosierra = new Equipable("Motosierra", "Pesada y ruidosa", Ranura.ARMA, 8, -4);
        Equipable campera = new Equipable("Campera de cuero", "Protege de los mordiscos", Ranura.ARMADURA, 0, 3);

        p.equipar(manopla);
        if (p.getAtaque() != sinNada.getAtaque() + 2) {
            throw new IllegalStateException("Equipar un arma no sumó su ataque");
        }
        if (p.equipar(motosierra) != manopla || p.getAtaque() != sinNada.getAtaque() + 8) {
            throw new IllegalStateException("Cambiar de arma debería reemplazar la anterior, no acumular");
        }
        p.equipar(campera);
        if (p.getDefensa() != sinNada.getDefensa() - 4 + 3) {
            throw new IllegalStateException("Arma y armadura deberían sumar sus bonus de defensa");
        }

        p.ganarExperiencia(p.experienciaParaSubir());
        sinNada.ganarExperiencia(sinNada.experienciaParaSubir());
        if (p.getAtaque() != sinNada.getAtaque() + 8) {
            throw new IllegalStateException("Subir de nivel no debería perder el bonus del equipamiento");
        }

        Medico m = new Medico("Dra. Vega");
        m.equipar(new Equipable("Prueba", "Resta mucha defensa", Ranura.ARMADURA, 0, -50));
        if (m.getDefensa() != 0) {
            throw new IllegalStateException("La defensa no debería quedar negativa");
        }

        Bombero rex = new Bombero("Rex");
        Equipo equipo = new Equipo(List.of(rex, new Medico("Dr. Ruiz")), new Inventario());
        equipo.getInventario().agregarEquipable(manopla);
        equipo.getInventario().agregarEquipable(motosierra);
        equipo.equipar(rex, manopla);
        equipo.equipar(rex, motosierra);
        if (rex.getEquipado(Ranura.ARMA) != motosierra
                || !equipo.getInventario().getEquipables().equals(List.of(manopla))) {
            throw new IllegalStateException("Al cambiar de arma, la anterior debería volver a la mochila");
        }

        FabricaDeEquipables fabrica = new FabricaDeEquipables(new GeneradorAleatorio(4));
        for (int i = 0; i < 50; i++) {
            String nombre = fabrica.crear(1).getNombre();
            if (!nombre.equals("Manopla") && !nombre.equals("Campera de cuero")) {
                throw new IllegalStateException("En el nivel 1 solo deberían salir piezas básicas, salió " + nombre);
            }
        }

        VistaFalsa vista = new VistaFalsa();
        ControladorJuego c = new ControladorJuego(vista, new GeneradorAleatorio(2));
        c.onNuevaPartida();
        c.onEquipoElegido(List.of(0, 5));
        c.onAbrirEquipamiento();
        if (vista.equipamiento != null) {
            throw new IllegalStateException("Con una batalla en curso no debería poder abrirse el equipamiento");
        }
        System.out.println("[9] Equipamiento OK");
    }
}
