#pragma once
// ============================================================================
//  Configuracion de hardware y calibracion de Lia
// ============================================================================

// 1 = dibujar ojos en dos pantallas redondas GC9A01 (240x240). 0 = solo servos.
#define USAR_OJOS 1

// 1 = version humanoide con piernas (10 servos DS3218 mas, segundo PCA9685).
// 0 = version de escritorio sobre la base.
#define CON_PATAS 1

// --- I2C hacia el PCA9685 ---
#define PIN_SDA 21
#define PIN_SCL 22
#define PIN_OE 27            // OE de ambos PCA9685 (LOW = salidas activas)
#define PCA9685_DIR 0x40     // canales 0-15
#define PCA9685_DIR2 0x41    // canales 16-31 (puente A0 soldado), solo con patas
#define SERVO_HZ 50

// --- SPI de los ojos (bus compartido, CS y RST separados) ---
#define PIN_SCK 18
#define PIN_MOSI 23
#define PIN_DC 16
#define PIN_CS_DER 17
#define PIN_CS_IZQ 5
#define PIN_RST_DER 25
#define PIN_RST_IZQ 26

// Periodo del lazo de movimiento
#define PERIODO_MS 20

// ---------------------------------------------------------------------------
//  Articulaciones
//  centro   : angulo de la articulacion cuando el servo recibe 1500 us
//  min/max  : limites de seguridad (ya verificados contra choques en el CAD)
//  reposo   : pose de montaje y descanso
//  invertido: cambiar si la articulacion se mueve al reves
//  usPorGrado: 2000 us / 180 grados para la mayoria de MG996R y MG90S
// ---------------------------------------------------------------------------
struct ConfigArticulacion {
  const char *nombre;
  uint8_t canal;
  float centro;
  float minimo;
  float maximo;
  float reposo;
  bool invertido;
  float usPorGrado;
  float velocidad;  // grados por segundo
};

enum {
  CUELLO_PAN = 0,
  CUELLO_TILT,
  HOMBRO_DER,
  ABDUC_DER,
  CODO_DER,
  HOMBRO_IZQ,
  ABDUC_IZQ,
  CODO_IZQ,
  N_SUP,
#if CON_PATAS
  // Piernas. Signos: roll + = el pie se aleja del centro; cadera + = muslo
  // adelante; rodilla + = flexiona; tobillo + = punta arriba.
  // Pie plano si tobillo = rodilla - cadera y roll_tobillo = -roll_cadera.
  ROLL_CAD_DER = N_SUP,
  CADERA_DER,
  RODILLA_DER,
  TOBILLO_DER,
  ROLL_TOB_DER,
  ROLL_CAD_IZQ,
  CADERA_IZQ,
  RODILLA_IZQ,
  TOBILLO_IZQ,
  ROLL_TOB_IZQ,
  N_ART
#else
  N_ART = N_SUP
#endif
};

#define N_PAT 10

static const ConfigArticulacion CONFIG_ART[N_ART] = {
  //  nombre         canal centro  min   max  reposo  inv    us/g   vel
  { "pan",            0,    0,   -70,   70,    0,  false, 11.11, 120 },
  { "tilt",           1,    0,   -15,   15,    0,  false, 11.11,  60 },
  { "hombro_der",     2,   65,   -25,  155,    0,  false, 11.11, 150 },
  { "abduc_der",      3,   45,     0,   90,    0,  false, 11.11, 150 },
  { "codo_der",       4,   55,     0,  110,    0,  false, 11.11, 200 },
  { "hombro_izq",     5,   65,   -25,  155,    0,  true,  11.11, 150 },
  { "abduc_izq",      6,   45,     0,   90,    0,  true,  11.11, 150 },
  { "codo_izq",       7,   55,     0,  110,    0,  true,  11.11, 200 },
#if CON_PATAS
  // DS3218 de 180 grados (para la version de 270 grados usar 7.41 us/grado)
  { "roll_cad_der",  16,    0,   -15,   15,    0,  false, 11.11,  40 },
  { "cadera_der",    17,   30,   -30,   90,    0,  false, 11.11,  60 },
  // Por encima de ~105 grados el muslo y la tibia se tocan.
  { "rodilla_der",   18,   55,     0,  100,    0,  false, 11.11,  60 },
  { "tobillo_der",   19,    0,   -30,   30,    0,  false, 11.11,  60 },
  { "roll_tob_der",  20,    0,   -20,   20,    0,  false, 11.11,  40 },
  { "roll_cad_izq",   8,    0,   -15,   15,    0,  true,  11.11,  40 },
  { "cadera_izq",     9,   30,   -30,   90,    0,  true,  11.11,  60 },
  { "rodilla_izq",   10,   55,     0,  100,    0,  true,  11.11,  60 },
  { "tobillo_izq",   11,    0,   -30,   30,    0,  true,  11.11,  60 },
  { "roll_tob_izq",  12,    0,   -20,   20,    0,  true,  11.11,  40 },
#endif
};

// Ajuste fino por servo (us) tras calibrar con el comando "us".
static const int16_t TRIM_US[N_ART] = { 0 };

#define US_MIN 500
#define US_MAX 2500
