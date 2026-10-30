#pragma once
// Ojos animados en dos pantallas redondas GC9A01 (1.28", 240x240) con la
// libreria "GFX Library for Arduino". Se dibuja directo en pantalla (sin
// framebuffer) para que funcione en un ESP32 sin PSRAM.

#include <Arduino_GFX_Library.h>
#include "config.h"
#include "gestos.h"

namespace ojos {

const uint16_t IRIS = 0x5D7F;     // celeste
const uint16_t PUPILA = 0x0862;
const uint16_t BRILLO = 0xFFFF;
const uint16_t FONDO = 0x0000;
const int16_t C = 120;
const int16_t R_IRIS = 78;
const int16_t R_PUPILA = 38;

Arduino_DataBus *busDer = new Arduino_HWSPI(PIN_DC, PIN_CS_DER, PIN_SCK, PIN_MOSI, GFX_NOT_DEFINED, &SPI, true);
Arduino_DataBus *busIzq = new Arduino_HWSPI(PIN_DC, PIN_CS_IZQ, PIN_SCK, PIN_MOSI, GFX_NOT_DEFINED, &SPI, true);
Arduino_GFX *der = new Arduino_GC9A01(busDer, PIN_RST_DER, 0, true);
Arduino_GFX *izq = new Arduino_GC9A01(busIzq, PIN_RST_IZQ, 0, true);

int16_t mx = 0, my = 0;            // mirada dibujada
Expresion expresion = EXP_NORMAL;
bool redibujar = true;
uint32_t proximoParpadeo = 0;
uint8_t faseParpadeo = 0;          // 0 = abierto
uint32_t tFase = 0;

void dibujarIris(Arduino_GFX *g, int16_t dx, int16_t dy) {
  g->fillCircle(C + dx, C + dy, R_IRIS, IRIS);
  g->fillCircle(C + dx, C + dy, R_PUPILA, PUPILA);
  g->fillCircle(C + dx + 22, C + dy - 24, 12, BRILLO);
  g->fillCircle(C + dx - 16, C + dy + 20, 5, BRILLO);
}

void dibujarOjo(Arduino_GFX *g, int16_t dx, int16_t dy) {
  g->fillScreen(FONDO);
  switch (expresion) {
    case EXP_FELIZ:
      // ojo "sonriente": arco grueso hacia arriba
      g->fillCircle(C + dx, C + 30 + dy, 80, IRIS);
      g->fillCircle(C + dx, C + 62 + dy, 76, FONDO);
      break;
    case EXP_DORMIDA:
      g->fillRoundRect(C - 70, C + 10, 140, 14, 7, IRIS);
      break;
    case EXP_SORPRESA:
      g->fillCircle(C + dx, C + dy, R_IRIS + 10, IRIS);
      g->fillCircle(C + dx, C + dy, R_PUPILA - 14, PUPILA);
      g->fillCircle(C + dx + 26, C + dy - 28, 12, BRILLO);
      break;
    default:
      dibujarIris(g, dx, dy);
      break;
  }
}

void dibujarAmbos() {
  dibujarOjo(der, mx, my);
  dibujarOjo(izq, mx, my);
}

// Mueve solo el iris: borra la posicion anterior con un anillo de fondo.
void moverIris(int16_t nx, int16_t ny) {
  for (Arduino_GFX *g : { der, izq }) {
    g->fillCircle(C + mx, C + my, R_IRIS + 1, FONDO);
    dibujarIris(g, nx, ny);
  }
  mx = nx;
  my = ny;
}

void parpados(int16_t cierre) {
  // cierre: 0 (abierto) .. 120 (cerrado), se dibuja desde arriba y abajo
  for (Arduino_GFX *g : { der, izq }) {
    g->fillRect(0, 0, 240, cierre, FONDO);
    g->fillRect(0, 240 - cierre, 240, cierre, FONDO);
  }
}

}  // namespace ojos

inline void ojosIniciar() {
  using namespace ojos;
  der->begin(40000000);
  izq->begin(40000000);
  proximoParpadeo = millis() + 3000;
  dibujarAmbos();
}

inline void ojosExpresion(Expresion e) {
  if (e == EXP_MANTENER) return;
  ojos::expresion = e;
  ojos::redibujar = true;
}

inline void ojosParpadear() {
  ojos::proximoParpadeo = millis();
}

// Llamar ~50 veces por segundo. (mirarX, mirarY) en pixeles, positivo = derecha/abajo.
inline void ojosActualizar(uint32_t ahora, float mirarX, float mirarY) {
  using namespace ojos;
  if (faseParpadeo == 0 && (int32_t)(ahora - proximoParpadeo) >= 0 && expresion != EXP_DORMIDA) {
    faseParpadeo = 1;
    tFase = ahora;
  }
  if (faseParpadeo > 0) {
    // cerrar en 4 pasos, abrir redibujando
    if (ahora - tFase >= 25) {
      tFase = ahora;
      if (faseParpadeo <= 4) {
        parpados(faseParpadeo * 30);
        faseParpadeo++;
      } else {
        faseParpadeo = 0;
        proximoParpadeo = ahora + random(2500, 6500);
        redibujar = true;
      }
    }
    if (faseParpadeo > 0) return;
  }

  if (redibujar) {
    redibujar = false;
    dibujarAmbos();
    return;
  }
  if (expresion != EXP_NORMAL) return;

  int16_t nx = (int16_t)constrain(mirarX, -30.0f, 30.0f);
  int16_t ny = (int16_t)constrain(mirarY, -24.0f, 24.0f);
  if (abs(nx - mx) >= 3 || abs(ny - my) >= 3) moverIris(nx, ny);
}
