# Lia: robot compañera imprimible en 3D

Lia es un robot social de escritorio inspirado en [Abi de Andromeda Robotics](https://andromedarobotics.ai/): cara amable con ojos en pantalla, cabeza que gira y asiente, y brazos expresivos para saludar o abrazar. No es una copia de Abi. Es un diseño propio, más pequeño (unos 40 cm), pensado para imprimirse en una impresora doméstica de 220 × 220 mm y armarse con servos de hobby.

<p align="center">
  <img src="imagenes/lia_frente.png" width="32%" alt="Lia de frente">
  <img src="imagenes/lia_saludo.png" width="32%" alt="Lia saludando">
  <img src="imagenes/lia_trasera.png" width="32%" alt="Lia por detrás">
</p>

| | |
|---|---|
| Altura | 397 mm |
| Ancho con brazos | 270 mm |
| Grados de libertad | 8 (cuello pan/tilt, 3 por brazo) |
| Servos | 4 × MG996R, 4 × MG90S |
| Control | ESP32 + PCA9685 (16 canales PWM) |
| Ojos | 2 pantallas redondas GC9A01 de 1.28" (240 × 240) |
| Plástico | ~1 kg de PLA o PETG |

## Estructura

```
robot-lia/
├── cad/
│   ├── config.scad        medidas de servos, tornillos y robot (editar aquí)
│   ├── lia.scad           archivo principal: ensamble o pieza a imprimir
│   └── lib/               base, torso, cuello, cabeza, brazos, servos
├── stl/                   piezas listas para el slicer (ya orientadas)
├── firmware/robot_lia/    sketch de Arduino para ESP32
├── herramientas/          exportar STL y chequeo de choques
└── imagenes/
```

## Cinemática y servos

![Piezas en orientación de impresión](imagenes/lia_piezas.png)

Cada servo queda en un alojamiento con la medida exacta del cuerpo. Las orejas de montaje apoyan sobre una placa interna y se atornillan con los tornillos que trae el servo. Las piezas móviles se atornillan a la bocina (el brazo del servo) y tienen un agujero en el eje para llegar al tornillo central.

| Canal | Articulación | Servo | Ubicación | Rango (°) | Carga estimada |
|---|---|---|---|---|---|
| 0 | `pan` (girar cabeza) | MG996R | techo del torso | −70 … 70 | solo inercia (~450 g) |
| 1 | `tilt` (asentir) | MG996R | bloque del cuello | −15 … 15 | ~0.9 kg·cm |
| 2 / 5 | `hombro_der/izq` (flexión) | MG996R | costados del torso | −25 … 155 | ~0.85 kg·cm |
| 3 / 6 | `abduc_der/izq` (abrir brazo) | MG90S | pieza `hombro` | 0 … 90 | ~0.65 kg·cm |
| 4 / 7 | `codo_der/izq` | MG90S | pieza `brazo` | 0 … 110 | ~0.1 kg·cm |

Las cargas están calculadas con las piezas impresas macizas y el brazo horizontal, que es el peor caso. Con 20 a 40 % de relleno pesan menos. El MG996R entrega ~10 kg·cm y el MG90S ~2 kg·cm a 6 V, así que el margen va de 3× (abducción) a más de 10× (hombro).

Los rangos de la tabla son los límites del firmware. Fueron verificados contra choques en el CAD con `herramientas/chequear_interferencias.sh`: servos dentro de sus alojamientos, cabeza contra cuello y torso al inclinarse, brazos contra torso y base, antebrazo contra brazo, y brazos levantados contra la cabeza girando ±70°.

### Antes de imprimir: mide tus servos

Los MG996R y MG90S genéricos varían algunas décimas de milímetro entre fabricantes. Con un calibre, revisa estos valores en `cad/config.scad`:

- `S_L`, `S_W`: largo y ancho del cuerpo.
- `S_TABD`: distancia de la cara superior del cuerpo a la cara superior de las orejas. Define el espesor de la placa de montaje y es la medida más importante.
- `S_SOFF`: distancia del eje al borde más cercano.
- `S_HORNH`, `S_HORNR`: altura de la cara exterior de la bocina y radio de sus agujeros.
- `EYE_PCB`: diámetro de tu módulo GC9A01.

Imprime primero `cuello` y `hombro_der` (son chicos) para probar el encaje. Si los servos entran muy apretados, sube `CLR` a 0.35 o 0.4.

## Impresión

Las piezas de `stl/` ya están orientadas: importa y lamina. Configuración base: capa de 0.2 mm, 4 perímetros, PLA o PETG.

| Pieza | Cant. | Relleno | Soportes | Notas |
|---|---|---|---|---|
| `base` | 1 | 15 % | no | cubierta superior en la cama |
| `base_tapa` | 1 | 20 % | no | |
| `torso` | 1 | 15 % | no | boca abajo; los chaflanes son de 45°; usa brim |
| `cuello` | 1 | 40 % | no | |
| `cuello_tapa` | 1 | 40 % | no | |
| `cabeza` | 1 | 15 % | sí, tipo árbol | solo para el domo trasero |
| `cara` | 1 | 15 % | no | el frente queda liso contra la cama |
| `hombro_der` / `hombro_izq` | 1 + 1 | 40 % | no | |
| `brazo_der` / `brazo_izq` | 1 + 1 | 40 % | no | |
| `antebrazo_der` / `antebrazo_izq` | 1 + 1 | 30 % | no | |

Para regenerar los STL después de cambiar medidas (requiere OpenSCAD 2021 o superior):

```bash
robot-lia/herramientas/exportar_stl.sh              # todas
robot-lia/herramientas/exportar_stl.sh torso cara   # solo algunas
robot-lia/herramientas/chequear_interferencias.sh   # verifica que nada choque
```

Para ver el ensamble en otra pose, abre `cad/lia.scad` en OpenSCAD y cambia `pan`, `tilt`, `der` e `izq` (por ejemplo `der = [150, 25, 70]` para saludar).

## Lista de materiales

**Electrónica**

- 4 × servo MG996R de engranajes metálicos, con bocina redonda
- 4 × servo MG90S de engranajes metálicos
- 1 × PCA9685, placa de 16 canales PWM por I²C (formato Adafruit, agujeros de 55.9 × 19 mm)
- 1 × ESP32 DevKit (30 o 38 pines)
- 2 × pantalla redonda GC9A01 de 1.28" SPI
- 1 × fuente de 6 V y 10 A con conector 5.5 × 2.1 mm, más su jack de panel
- 1 × regulador reductor a 5 V (MP1584 o LM2596) para el ESP32
- 1 × interruptor redondo de 12 mm
- 1 × condensador de 1000 µF y 16 V en la entrada V+ del PCA9685
- 6 × extensión de cable para servo (15 a 30 cm)
- Opcional para voz: parlante de 40 mm y 3 W, amplificador I²S MAX98357A y micrófono I²S INMP441

**Tornillería**

- 4 × M3 × 10: base al torso
- 4 × M3 × 8 avellanados: tapa inferior de la base
- 4 × M3 × 12: tapa del cuello
- 1 × M4 × 16, 1 tuerca M4 y 2 arandelas: eje loco de la cabeza
- ~30 × M2 × 8 autorroscantes: bocinas y servos MG90S
- 4 × M2.5 × 6 autorroscantes: PCA9685
- 8 × imán de neodimio de 6 × 3 mm: cara a cabeza
- 4 × pata de goma de 12 mm
- Bridas plásticas

## Conexiones

```
Fuente 6 V ──┬── interruptor ──┬── PCA9685 V+ / GND (bornera, cable de 1 mm² o más)
             │                 └── regulador 5 V ── ESP32 VIN / GND
             └── GND común para todo

ESP32           PCA9685            GC9A01 derecho     GC9A01 izquierdo
3V3  ────────── VCC  ───────────── VCC ────────────── VCC
GND  ────────── GND  ───────────── GND ────────────── GND
GPIO21 ──────── SDA
GPIO22 ──────── SCL
GPIO27 ──────── OE
GPIO18 ─────────────────────────── SCL (SCK) ──────── SCL (SCK)
GPIO23 ─────────────────────────── SDA (MOSI) ─────── SDA (MOSI)
GPIO16 ─────────────────────────── DC ─────────────── DC
GPIO17 ─────────────────────────── CS
GPIO5  ──────────────────────────────────────────────── CS
GPIO25 ─────────────────────────── RST
GPIO26 ──────────────────────────────────────────────── RST
```

Los servos van a los canales 0 a 7 del PCA9685, en el orden de la tabla de cinemática. No alimentes los servos desde el ESP32 ni desde el USB: con carga, cuatro MG996R pueden pedir más de 8 A en un instante.

## Firmware

Requiere el core ESP32 para Arduino (probado con 3.3) y estas dos librerías del gestor de Arduino:

- Adafruit PWM Servo Driver Library
- GFX Library for Arduino

```bash
arduino-cli compile --fqbn esp32:esp32:esp32 robot-lia/firmware/robot_lia
arduino-cli upload  --fqbn esp32:esp32:esp32 -p /dev/ttyUSB0 robot-lia/firmware/robot_lia
```

Si todavía no tienes las pantallas, pon `#define USAR_OJOS 0` en `config.h`.

Abre el monitor serie a 115200 baudios con fin de línea "nueva línea":

| Comando | Qué hace |
|---|---|
| `ayuda` | lista de comandos |
| `gesto saludar` | también `asentir`, `negar`, `abrazar`, `pensar`, `bailar`, `feliz` |
| `pose abrazo` | también `reposo`, `brazos_arriba` |
| `j hombro_der 90` | mueve una articulación (respeta los límites) |
| `ojos feliz` | también `normal`, `sorpresa`, `dormida`, `parpadear` |
| `vida on` / `vida off` | micro-movimientos y parpadeos espontáneos cuando está quieta |
| `vel 0.5` | más lenta (0.2 a 2.0) |
| `montaje` | todos los servos a la pose de reposo, para fijar las bocinas |
| `us 2 1500` | pulso crudo a un canal, para calibrar |
| `relajar` | apaga todos los servos |

Los movimientos aceleran y frenan suavemente, y cada articulación tiene su velocidad máxima. Al encender, los servos se activan de a uno para no provocar un pico de corriente. Los ojos se adelantan hacia donde gira la cabeza, como hace una persona.

## Armado

La pose de reposo es la de referencia para todo: brazos colgando pegados al cuerpo, codos estirados y cabeza mirando al frente. Las bocinas se montan con los servos ya energizados en esa pose.

1. **Electrónica.** Atornilla el PCA9685 y fija el ESP32 y el regulador con bridas en `base_tapa`. Monta el parlante detrás de la rejilla frontal y el jack y el interruptor en la parte trasera. Carga el firmware.
2. **Servos sin bocina.** Conecta los 8 servos, envía `montaje` y espera a que todos se detengan.
3. **Torso.** Mete el servo `pan` por abajo hasta el techo y los dos servos de hombro hacia los costados. Atornilla las orejas desde adentro con un destornillador corto o en L. Pasa los cables a la base y atornilla el torso a la base (M3 × 10 desde abajo).
4. **Cuello.** Atornilla la bocina redonda del MG996R a la cara inferior del `cuello`, con tornillos M2 que entran desde el lado de la bocina. Calza el conjunto en el eje pan con el cuello mirando al frente y pon el tornillo central pasando el destornillador por el agujero superior. Luego mete el servo tilt desde el lado abierto, atorníllalo, pon su bocina en el eje y cierra con `cuello_tapa` (4 × M3 × 12). Antes, coloca la tuerca M4 en su hexágono, por dentro de la tapa.
5. **Cabeza.** Pega las pantallas en los portaojos de la `cara` y pega los imanes en la cara y en la cabeza, revisando la polaridad antes de que seque el pegamento. Baja la cabeza sobre el cuello por la abertura del piso. Con la cara sacada, atornilla la oreja izquierda de Lia a la bocina tilt (4 × M2 desde afuera de la oreja) y pasa el M4 × 16 con arandela por la oreja derecha hasta la tuerca de la tapa. Izquierda y derecha son siempre las del robot, mirándolo de frente quedan al revés. Los cables bajan por la abertura del piso y entran al torso por el agujero del techo.
6. **Brazos.** Atornilla una bocina MG996R a la cara interior de cada `hombro`, mete el MG90S de abducción por detrás y calza el hombro en el eje del torso con el brazo colgando. Repite para `brazo` (bocina MG90S en su cara trasera, servo del codo por el lado interior) y para `antebrazo`. Cada pieza tiene un agujero en el eje para el tornillo central. Pasa los cables por el canal interior del brazo y entra al torso por el agujero detrás del hombro, dejando holgura para el movimiento.
7. **Calibración.** Prueba cada articulación con `j <nombre> <grados>` y revisa con `estado`. Si alguna se mueve al revés, cambia `invertido` en `config.h`. Si en reposo queda torcida unos grados, corrige `TRIM_US` (unos 11 µs equivalen a 1°). Recién después prueba los gestos.

## Seguridad

- Los primeros encendidos hazlos con `vel 0.5` y con las manos lejos de los hombros y el cuello, que son los puntos de pellizco.
- Si algo choca o zumba, envía `relajar` o corta el interruptor. Un servo trabado se calienta en segundos.
- Para un robot de compañía cerca de personas mayores, mantén velocidades bajas y deja `vida on` solo con movimientos pequeños, como los que ya trae.

## Próximos pasos sugeridos

- **Voz y conversación.** Con el micrófono INMP441 y el MAX98357A en la base, el ESP32 puede enviar audio por WiFi a un servicio de reconocimiento de voz y a un modelo de lenguaje, y luego disparar gestos y expresiones según la respuesta. Para hacerlo todo en el robot conviene un ESP32-S3 con PSRAM.
- **Sensor táctil.** Un TTP223 bajo la tapa de la cabeza permite reaccionar a una caricia.
- **Base móvil.** La base de 190 mm puede montarse sobre una plataforma diferencial con dos motores y ruedas locas.
