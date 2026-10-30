// ============================================================================
//  Lia - robot companera: control de 8 servos con ESP32 + PCA9685
//
//  Monitor serie a 115200 baudios, fin de linea "nueva linea". Escribe "ayuda".
// ============================================================================

#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>
#include "config.h"
#include "gestos.h"
#if USAR_OJOS
#include "ojos.h"
#endif

Adafruit_PWMServoDriver pca(PCA9685_DIR);

struct Articulacion {
  float actual;
  float objetivo;
  float velocidad;
  bool activa;
};

Articulacion art[N_ART];

const Gesto *gestoActual = nullptr;
uint8_t pasoGesto = 0;
uint32_t finPaso = 0;
bool modoVida = true;
uint32_t proximaVida = 0;
uint32_t ultimoComando = 0;
float factorVelocidad = 1.0f;

// ---------------------------------------------------------------------------
//  Servos
// ---------------------------------------------------------------------------

float limitar(uint8_t i, float grados) {
  return constrain(grados, CONFIG_ART[i].minimo, CONFIG_ART[i].maximo);
}

uint16_t gradosAMicros(uint8_t i, float grados) {
  const ConfigArticulacion &c = CONFIG_ART[i];
  float delta = (grados - c.centro) * (c.invertido ? -1.0f : 1.0f);
  float us = 1500.0f + delta * c.usPorGrado + TRIM_US[i];
  return (uint16_t)constrain(us, (float)US_MIN, (float)US_MAX);
}

void escribirServo(uint8_t i) {
  if (!art[i].activa) return;
  pca.writeMicroseconds(CONFIG_ART[i].canal, gradosAMicros(i, art[i].actual));
}

void relajar(uint8_t i) {
  art[i].activa = false;
  pca.setPWM(CONFIG_ART[i].canal, 0, 4096);  // salida totalmente apagada
}

void mover(uint8_t i, float grados, float vel = -1) {
  art[i].objetivo = limitar(i, grados);
  art[i].velocidad = vel > 0 ? vel : CONFIG_ART[i].velocidad;
  art[i].activa = true;
}

// Acercamiento suave: desacelera cerca del objetivo y respeta la velocidad maxima.
void actualizarServos(float dt) {
  for (uint8_t i = 0; i < N_ART; i++) {
    if (!art[i].activa) continue;
    float err = art[i].objetivo - art[i].actual;
    if (fabsf(err) < 0.05f) {
      art[i].actual = art[i].objetivo;
    } else {
      float maxPaso = art[i].velocidad * factorVelocidad * dt;
      float paso = err * 0.2f;
      if (fabsf(paso) < 0.3f) paso = err > 0 ? min(0.3f, err) : max(-0.3f, err);
      art[i].actual += constrain(paso, -maxPaso, maxPaso);
    }
    escribirServo(i);
  }
}

bool todasEnObjetivo() {
  for (uint8_t i = 0; i < N_ART; i++)
    if (art[i].activa && fabsf(art[i].objetivo - art[i].actual) > 1.0f) return false;
  return true;
}

void aplicarPose(const int16_t *pose, float vel = -1) {
  for (uint8_t i = 0; i < N_ART; i++)
    if (pose[i] != MANTENER) mover(i, pose[i], vel);
}

void poseReposo() {
  for (uint8_t i = 0; i < N_ART; i++) mover(i, CONFIG_ART[i].reposo);
}

// ---------------------------------------------------------------------------
//  Gestos (secuencias no bloqueantes)
// ---------------------------------------------------------------------------

void iniciarGesto(const Gesto *g) {
  gestoActual = g;
  pasoGesto = 0;
  finPaso = 0;
  Serial.printf("gesto: %s\n", g->nombre);
}

void actualizarGesto(uint32_t ahora) {
  if (!gestoActual) return;
  if (finPaso != 0 && (int32_t)(ahora - finPaso) < 0) return;
  if (pasoGesto >= gestoActual->nPasos) {
    gestoActual = nullptr;
    poseReposo();
    return;
  }
  const PasoGesto &p = gestoActual->pasos[pasoGesto++];
  aplicarPose(p.pose, p.velocidad > 0 ? p.velocidad : -1);
#if USAR_OJOS
  if (p.expresion != EXP_MANTENER) ojosExpresion((Expresion)p.expresion);
#endif
  finPaso = ahora + p.duracionMs;
}

// Pequeños movimientos espontaneos cuando no hay ordenes: hace que se sienta viva.
void actualizarVida(uint32_t ahora) {
  if (!modoVida || gestoActual || ahora - ultimoComando < 4000) return;
  if ((int32_t)(ahora - proximaVida) < 0) return;
  proximaVida = ahora + random(2500, 7000);
  switch (random(0, 6)) {
    case 0: case 1:
      mover(CUELLO_PAN, random(-25, 26), 40);
      mover(CUELLO_TILT, random(-6, 9), 25);
      break;
    case 2:
      mover(CUELLO_PAN, 0, 40);
      mover(CUELLO_TILT, 0, 25);
      break;
    case 3:
      mover(ABDUC_DER, random(0, 12), 30);
      mover(ABDUC_IZQ, random(0, 12), 30);
      break;
    default:
#if USAR_OJOS
      ojosParpadear();
#endif
      break;
  }
}

// ---------------------------------------------------------------------------
//  Consola serie
// ---------------------------------------------------------------------------

int buscarArticulacion(const String &s) {
  if (s.length() > 0 && isDigit(s[0])) {
    int i = s.toInt();
    return (i >= 0 && i < N_ART) ? i : -1;
  }
  for (uint8_t i = 0; i < N_ART; i++)
    if (s.equalsIgnoreCase(CONFIG_ART[i].nombre)) return i;
  return -1;
}

void imprimirAyuda() {
  Serial.println(F(
    "Comandos:\n"
    "  ayuda                    esta lista\n"
    "  estado                   angulos actuales y objetivos\n"
    "  j <art> <grados>         mover una articulacion (nombre o numero)\n"
    "  pose <nombre>            reposo | brazos_arriba | abrazo\n"
    "  gesto <nombre>           saludar | asentir | negar | abrazar | pensar | bailar | feliz\n"
    "  vel <factor>             multiplicador de velocidad (0.2 - 2.0)\n"
    "  vida on|off              movimientos espontaneos\n"
    "  montaje                  todos los servos a la pose de reposo (para fijar bocinas)\n"
    "  us <canal> <micros>      pulso crudo para calibrar (500-2500)\n"
    "  relajar                  apaga todos los servos\n"
#if USAR_OJOS
    "  ojos <expresion>         normal | feliz | sorpresa | dormida | parpadear\n"
#endif
  ));
}

void imprimirEstado() {
  for (uint8_t i = 0; i < N_ART; i++)
    Serial.printf("%d %-11s act=%7.1f obj=%7.1f us=%4u %s\n", i, CONFIG_ART[i].nombre,
                  art[i].actual, art[i].objetivo, gradosAMicros(i, art[i].actual),
                  art[i].activa ? "" : "(relajado)");
}

void procesarComando(String linea) {
  linea.trim();
  if (linea.length() == 0) return;
  ultimoComando = millis();

  int sp = linea.indexOf(' ');
  String cmd = sp < 0 ? linea : linea.substring(0, sp);
  String resto = sp < 0 ? "" : linea.substring(sp + 1);
  resto.trim();
  cmd.toLowerCase();

  if (cmd == "ayuda" || cmd == "help" || cmd == "?") {
    imprimirAyuda();
  } else if (cmd == "estado") {
    imprimirEstado();
  } else if (cmd == "j") {
    int sp2 = resto.indexOf(' ');
    int i = buscarArticulacion(sp2 < 0 ? resto : resto.substring(0, sp2));
    if (i < 0 || sp2 < 0) { Serial.println(F("uso: j <art> <grados>")); return; }
    float g = resto.substring(sp2 + 1).toFloat();
    gestoActual = nullptr;
    mover(i, g);
    Serial.printf("%s -> %.1f\n", CONFIG_ART[i].nombre, art[i].objetivo);
  } else if (cmd == "pose") {
    const Pose *p = buscarPose(resto.c_str());
    if (!p) { Serial.println(F("pose desconocida")); return; }
    gestoActual = nullptr;
    aplicarPose(p->valores);
  } else if (cmd == "gesto") {
    const Gesto *g = buscarGesto(resto.c_str());
    if (!g) { Serial.println(F("gesto desconocido")); return; }
    iniciarGesto(g);
  } else if (cmd == "vel") {
    factorVelocidad = constrain(resto.toFloat(), 0.2f, 2.0f);
    Serial.printf("velocidad x%.2f\n", factorVelocidad);
  } else if (cmd == "vida") {
    modoVida = resto.equalsIgnoreCase("on");
    Serial.printf("vida %s\n", modoVida ? "activada" : "desactivada");
  } else if (cmd == "montaje") {
    gestoActual = nullptr;
    modoVida = false;
    for (uint8_t i = 0; i < N_ART; i++) {
      art[i].actual = art[i].objetivo = CONFIG_ART[i].reposo;
      art[i].activa = true;
      escribirServo(i);
    }
    Serial.println(F("servos en reposo: coloca las bocinas con la pose de reposo (brazos abajo, cabeza al frente)"));
  } else if (cmd == "us") {
    int sp2 = resto.indexOf(' ');
    if (sp2 < 0) { Serial.println(F("uso: us <canal> <micros>")); return; }
    int canal = resto.substring(0, sp2).toInt();
    int us = constrain(resto.substring(sp2 + 1).toInt(), US_MIN, US_MAX);
    for (uint8_t i = 0; i < N_ART; i++)
      if (CONFIG_ART[i].canal == canal) art[i].activa = false;
    pca.writeMicroseconds(canal, us);
    Serial.printf("canal %d = %d us\n", canal, us);
  } else if (cmd == "relajar") {
    gestoActual = nullptr;
    for (uint8_t i = 0; i < N_ART; i++) relajar(i);
    Serial.println(F("servos relajados"));
#if USAR_OJOS
  } else if (cmd == "ojos") {
    if (resto == "parpadear") ojosParpadear();
    else ojosExpresion(expresionPorNombre(resto.c_str()));
#endif
  } else {
    Serial.println(F("comando desconocido, escribe 'ayuda'"));
  }
}

void leerSerie() {
  static String buffer;
  while (Serial.available()) {
    char c = Serial.read();
    if (c == '\n' || c == '\r') {
      procesarComando(buffer);
      buffer = "";
    } else if (buffer.length() < 80) {
      buffer += c;
    }
  }
}

// ---------------------------------------------------------------------------

void setup() {
  Serial.begin(115200);
  pinMode(PIN_OE, OUTPUT);
  digitalWrite(PIN_OE, HIGH);  // salidas apagadas hasta tener pulsos validos

  Wire.begin(PIN_SDA, PIN_SCL);
  pca.begin();
  pca.setOscillatorFrequency(27000000);
  pca.setPWMFreq(SERVO_HZ);

  // Arranque en reposo; se activan uno a uno para no exigir picos de corriente.
  for (uint8_t i = 0; i < N_ART; i++) pca.setPWM(CONFIG_ART[i].canal, 0, 4096);
  digitalWrite(PIN_OE, LOW);
  for (uint8_t i = 0; i < N_ART; i++) {
    art[i].actual = art[i].objetivo = CONFIG_ART[i].reposo;
    art[i].velocidad = CONFIG_ART[i].velocidad;
    art[i].activa = true;
    escribirServo(i);
    delay(200);
  }

#if USAR_OJOS
  ojosIniciar();
#endif
  randomSeed(esp_random());
  Serial.println(F("\nLia lista. Escribe 'ayuda'."));
  iniciarGesto(buscarGesto("saludar"));
}

void loop() {
  static uint32_t ultimo = millis();
  uint32_t ahora = millis();
  leerSerie();
  if (ahora - ultimo >= PERIODO_MS) {
    float dt = (ahora - ultimo) / 1000.0f;
    ultimo = ahora;
    actualizarGesto(ahora);
    actualizarVida(ahora);
    actualizarServos(dt);
#if USAR_OJOS
    // los ojos se adelantan hacia donde gira la cabeza
    float mx = (art[CUELLO_PAN].objetivo - art[CUELLO_PAN].actual) * 1.2f;
    float my = (art[CUELLO_TILT].actual - art[CUELLO_TILT].objetivo) * 2.0f;
    ojosActualizar(ahora, mx, my);
#endif
  }
}
