# RPG por turnos — Sobrevivientes vs. Zombies (base del proyecto)

Base programada a partir del diagrama de clases y del informe de la Fase A (Grupo 8), con
interfaz gráfica Swing y arquitectura **Modelo – DTO – Controlador – Vista**.
Requiere **Java 17 o superior** (usa `record`).

## Cómo correrlo

```
./compilar.sh      # compila a la carpeta out/
./jugar.sh         # abre la ventana del juego
./test.sh          # pruebas sin interfaz (simulación de partidas, guardado, turnos, contagio, niveles)
```
En Windows: `compilar.bat`, `jugar.bat`, `test.bat`.
En un IDE (Eclipse / IntelliJ / NetBeans): importar como proyecto Java con `src` como carpeta de fuentes
y ejecutar `rpg.Main`. `test/` es una segunda carpeta de fuentes (ejecutar `rpg.SimulacionTest`).
Las partidas guardadas quedan en `~/.rpg-sobrevivientes/partidas/` (carpeta de usuario, igual desde cualquier lugar donde se ejecute) (una por archivo `.sav`); "Cargar partida" deja elegir cuál. Se puede guardar entre batallas y en plena batalla (botón "Menú").

## Clases del informe y su equivalencia con el enunciado

| Informe | Enunciado | Rol | Habilidad |
|---|---|---|---|
| Bombero | Guerrero | mucha vida/defensa | Hachazo (x1.5 de daño) |
| Militar | Arquero | rápido, ataque alto | Disparo certero (ignora defensa) |
| Ingeniero | Mago | mucho maná | Descarga eléctrica (daño en área) |
| Médico | Curador | healer | Curación (a un aliado) |

Enemigos: **Zombie Común**, **Zombie Tanque** (a veces se cubre), **Zombie Rápido** (siempre ataca al
más débil). Cualquiera puede aparecer **infectado** (los morados del informe).

## Estructura

```
src/rpg/
  Main.java                 arranca Swing, crea vista + controlador
  modelo/                   TODO el dominio. No importa nada de vista, controlador, dto ni persistencia.
    unidad/                 Unidad, Personaje, Enemigo, Bombero, Militar, Ingeniero, Medico,
                            Zombie, ZombieComun, ZombieTanque, ZombieRapido, CalculadorDeDano, SelectorDeObjetivo
    habilidad/              Habilidad, Objetivo, AtaqueFuerte, DisparoCertero, Descarga, Curacion
    objeto/                 Objeto, Inventario, Botiquin, Suero, Granada, Trampa
    accion/                 Accion, AtacarAccion, DefenderAccion, UsarHabilidadAccion, UsarObjetoAccion
    batalla/                Batalla, OrquestadorDeTurnos, ResultadoBatalla,
                            RegistroDeCombate, EventoCombate, TipoEvento, ObservadorDeCombate
    partida/                Partida, Nivel, Oleada, Equipo, GestorDeEquipo,
                            EstadoPartida, EnMenu, EnBatalla, EntreBatallas, Finalizada
    generacion/             FabricaDeEnemigos, CurvaDeDificultad, GeneradorAleatorio
  persistencia/             Persistencia (guardar / cargar en archivo; infraestructura, fuera del dominio)
  dto/                      records inmutables que viajan modelo -> vista
  controlador/
    ControladorJuego.java   rol de "Juego" del diagrama. Conduce la batalla, guarda y carga.
    ConversorDTO.java       único lugar donde se traduce modelo -> DTO
  vista/
    IVista.java             lo que el controlador le puede pedir a una vista
    VistaSwing.java         JFrame con CardLayout (reemplaza a VistaConsola)
    PanelMenu / PanelSeleccion / PanelBatalla / PanelResultado / TarjetaUnidad
test/rpg/SimulacionTest.java
```

### Reglas de dependencia (importante para el equipo)
- `modelo` no conoce a nadie (los subpaquetes de `modelo` sí se referencian entre sí).
- `persistencia` solo conoce `modelo`.
- `vista` solo importa `dto` y `controlador` (nunca `modelo`).
- `controlador` conoce `modelo`, `persistencia`, `dto` e `IVista` (nunca clases Swing).
- Si el modelo cambia, se ajusta `ConversorDTO`; la vista casi no se entera.

### Cómo fluye una batalla
1. `Main` → `controlador.iniciar()` → vista muestra el menú.
2. "Nueva partida" → `onNuevaPartida()` → selección de 2 a 4 personajes con `List<UnidadDTO>`.
3. "Comenzar" → `onEquipoElegido(indices)` → se crea Equipo, Partida y primera Batalla.
4. `ControladorJuego` delega en `ControladorBatalla.jugar(partida, batalla)`. `avanzarBatalla()` deja jugar a la IA
   con `batalla.jugarTurnoAutomatico()` mientras `!batalla.esTurnoDelJugador()` (decide el `Bando`, sin `instanceof`)
   y se frena cuando le toca al jugador; ahí llama `vista.mostrarBatalla(BatallaDTO)`.
5. El jugador elige acción → la vista llama `onAtacar / onUsarHabilidad / onUsarObjeto / onDefender`
   con **índices** (vienen en los DTO) → `ControladorBatalla` arma la `Accion` → `Batalla.ejecutarTurno()` la **valida**
   (si es inválida lanza `AccionInvalidaException`, se muestra el mensaje y el turno no se consume) → vuelve al paso 4.
6. Fin de batalla → recompensas → `EstadoPartida.actualizar()` → `mostrarResultado()` →
   "Guardar partida" (solo entre batallas) y `onContinuar()`.

### Patrones usados (los del informe)
- **Factory:** `FabricaDeEnemigos` + `CurvaDeDificultad` (cantidad, stats +10%..30% en nivel 3, probabilidad de tanque/infeccioso).
- **Observador:** `RegistroDeCombate` (sujeto) emite `EventoCombate`; el controlador suscribe a la vista con
  `IVista.mostrarEvento`. El dominio nunca conoce la vista.
- **State:** `EnMenu / EnBatalla / EntreBatallas / Finalizada`.
- **Polimorfismo de acciones:** el `Orquestador` ejecuta cualquier `Accion` sin saber cuál es.
- `GeneradorAleatorio` separado: se puede reemplazar con semilla fija en las pruebas.

## Reglas del informe implementadas

- Party de 2 a 4 personajes; stats: nombre, vida, maná, ataque, defensa, velocidad, nivel, experiencia.
- Turnos por velocidad, **intercalando bandos**: nadie del mismo equipo actúa dos veces seguidas mientras
  el otro tenga unidades sin actuar en la ronda.
- Acciones: atacar, defender, habilidad, objeto (usar objeto consume el turno).
- Habilidades consumen maná; el costo y el poder suben con el nivel. El maná se recupera 3 por turno
  (+5 al defender) y se llena entre batallas.
- Objetos compartidos: **Botiquín** (10%-40% de vida, al azar), **Suero** (quita la infección; si no hay,
  5%-15% de vida), **Granada** (daño base a todos los enemigos), **Trampa** (el enemigo más lento pierde 2 turnos).
- **Infección:** al infectarse, la unidad queda **bloqueada 2 turnos** (`Unidad.TURNOS_BLOQUEO_INFECCION`, la vista
  muestra BLOQUEADO(n)); después puede actuar pero sigue infectada. Hace 5% de la vida máxima por turno hasta matarla
  o hasta que le den un Suero (que quita infección y bloqueo); si una unidad infectada muere, contagia a otra unidad viva
  (aliada o enemiga) al azar.
- Experiencia y niveles (cada clase crece distinto), botín de objetos al ganar, progresión de dificultad por nivel.
- **Niveles infinitos** (2 oleadas cada uno, generados a medida que se avanza): la partida solo termina cuando cae el equipo.
  Desde el nivel 5 siempre hay 4 zombies; las probabilidades de tanque/infectado tienen techo y las stats siguen subiendo.
- **Guardar / cargar** en archivo (`partida.sav`), solo entre batallas.

## Cambios al diagrama (hay que actualizarlo)

| Cambio | Por qué |
|---|---|
| `Juego` → `ControladorJuego`; `VistaConsola` → `VistaSwing` + `IVista`; paquete `dto` | Pedido del TP |
| Bucle por eventos (no bloquea esperando input) | Swing |
| `Combatiente` → `Bombero`; nuevas `Militar` y clases de zombie `ZombieTanque`; se elimina `ZombieInfeccioso` | Informe: la infección es un estado, no un tipo |
| `Personaje`: `mana`, `manaMaximo`, `nivel`, `experiencia`; `Unidad`: `infectado`, `defendiendo`, `turnosSalteados` | Enunciado e informe |
| `Habilidad.costoTurnos` → `costoMana` + `nivel` + `Objetivo`; `ejecutar(l, o, Batalla)` | Maná y habilidades en área |
| `Objeto.usar(usuario, objetivo, Batalla)`; nuevos `Granada`, `Trampa` (ahora son objetos, no habilidad); `Objetivo` (enum) | Informe |
| `Accion.objetivo` | Faltaba de dónde sacar el objetivo |
| `RegistroDeCombate` observable + `EventoCombate` + `ObservadorDeCombate` | Diagrama 2 del informe |
| `CurvaDeDificultad` | Diagrama 1 del informe |
| `EntreBatallas` (estado), `Persistencia`, `Partida.otorgarRecompensas()` | Guardado y recompensas |
| `OrquestadorDeTurnos`: rondas con bandos intercalados | Mecánica del informe |
| Todo el modelo es `Serializable` | Para guardar la partida |


## Pendientes sugeridos (para repartir)

- [ ] Sprites 8 bits y fondo de arena (`PanelBatalla` pintando imágenes)
- [ ] Elegir objetivo haciendo click en la tarjeta en vez de diálogos
- [ ] Balanceo: con un jugador al azar gana siempre con 4 personajes, casi siempre con 3 y rara vez con 2
      (conviene que la cantidad de zombies dependa del tamaño del equipo)
- [x] Equipables (armas y armaduras) como recompensa: se equipan entre batallas desde `ControladorEquipamiento`
- [ ] Tests unitarios del modelo con JUnit
- [ ] Actualizar diagrama de clases y diagramas de secuencia con los cambios de arriba
