#pragma once
// Poses y gestos. Orden de los valores del cuerpo superior:
//   pan, tilt, hombro_der, abduc_der, codo_der, hombro_izq, abduc_izq, codo_izq
// Piernas (opcional, al final de cada paso):
//   roll_cad, cadera, rodilla, tobillo, roll_tob  derecha y luego izquierda
// Un paso sin datos de piernas no las mueve. MANTENER deja la articulacion donde esta.

#include <string.h>
#include "config.h"

#define MANTENER (-999)

enum Expresion : uint8_t { EXP_NORMAL = 0, EXP_FELIZ, EXP_SORPRESA, EXP_DORMIDA, EXP_MANTENER = 255 };

struct Pose {
  const char *nombre;
  int16_t valores[N_SUP];
  bool conPatas;
  int16_t patas[N_PAT];
};

struct PasoGesto {
  int16_t pose[N_SUP];
  uint16_t duracionMs;
  float velocidad;       // <= 0 usa la velocidad de cada articulacion
  uint8_t expresion;
  bool conPatas;
  int16_t patas[N_PAT];
};

struct Gesto {
  const char *nombre;
  const PasoGesto *pasos;
  uint8_t nPasos;
};

#define M MANTENER

// Piernas: de pie con rodillas algo flexionadas (mas estable que rectas).
#define P_DE_PIE   0, 15, 30, 15, 0,   0, 15, 30, 15, 0
#define P_RECTA    0,  0,  0,  0, 0,   0,  0,  0,  0, 0
#define P_AGACHADA 0, 50, 80, 30, 0,   0, 50, 80, 30, 0
// Peso sobre una pierna: el cuerpo se corre sobre ese pie.
#define P_PESO_DER -10, 15, 30, 15, 10,   10, 15, 30, 15, -10
#define P_PESO_IZQ  10, 15, 30, 15, -10, -10, 15, 30, 15, 10

static const Pose POSES[] = {
  { "reposo",        { 0, 0,   0,  0,  0,   0,  0,  0 } },
  { "brazos_arriba", { 0, 8, 150, 30, 20, 150, 30, 20 } },
  { "abrazo",        { 0, 5,  80, 35, 45,  80, 35, 45 } },
#if CON_PATAS
  { "de_pie",        { 0, 0,   0,  0,  0,   0,  0,  0 }, true, { P_DE_PIE } },
  { "recta",         { 0, 0,   0,  0,  0,   0,  0,  0 }, true, { P_RECTA } },
  { "agachada",      { 0, 8,  40, 20, 30,  40, 20, 30 }, true, { P_AGACHADA } },
#endif
};

static const PasoGesto G_SALUDAR[] = {
  { { 10,  6, 150, 25, 60, M, M, M },  900, -1, EXP_FELIZ },
  { {  M,  M,   M,  M, 20, M, M, M },  350, -1, EXP_MANTENER },
  { {  M,  M,   M,  M, 85, M, M, M },  350, -1, EXP_MANTENER },
  { {  M,  M,   M,  M, 20, M, M, M },  350, -1, EXP_MANTENER },
  { {  M,  M,   M,  M, 85, M, M, M },  350, -1, EXP_MANTENER },
  { {  M,  M,   M,  M, 40, M, M, M },  400, -1, EXP_MANTENER },
  { {  0,  0,   0,  0,  0, M, M, M }, 1000, -1, EXP_NORMAL },
};

static const PasoGesto G_ASENTIR[] = {
  { { M,  12, M, M, M, M, M, M }, 350, 70, EXP_MANTENER },
  { { M,  -8, M, M, M, M, M, M }, 350, 70, EXP_MANTENER },
  { { M,  12, M, M, M, M, M, M }, 350, 70, EXP_MANTENER },
  { { M,   0, M, M, M, M, M, M }, 400, 70, EXP_MANTENER },
};

static const PasoGesto G_NEGAR[] = {
  { {  30, M, M, M, M, M, M, M }, 450, 110, EXP_MANTENER },
  { { -30, M, M, M, M, M, M, M }, 650, 110, EXP_MANTENER },
  { {  25, M, M, M, M, M, M, M }, 600, 110, EXP_MANTENER },
  { {   0, M, M, M, M, M, M, M }, 500, 110, EXP_MANTENER },
};

static const PasoGesto G_ABRAZAR[] = {
  { { 0, 8, 85, 50, 20, 85, 50, 20 }, 1200, 80, EXP_FELIZ },
  { { M, M, 85, 20, 70, 85, 20, 70 }, 1800, 60, EXP_MANTENER },
  { { 0, 0,  0,  0,  0,  0,  0,  0 }, 1400, 80, EXP_NORMAL },
};

static const PasoGesto G_PENSAR[] = {
  { { 20, 10, 95, 0, 110, M, M, M }, 1200, 80, EXP_NORMAL },
  { { 25, 12,  M, M,   M, M, M, M }, 2200, 20, EXP_MANTENER },
  { {  0,  0,  0, 0,   0, M, M, M }, 1200, 80, EXP_MANTENER },
};

static const PasoGesto G_BAILAR[] = {
  { {  20,  5, 120, 30, 60,  20, 10, 30 }, 500, -1, EXP_FELIZ },
  { { -20, -5,  20, 10, 30, 120, 30, 60 }, 500, -1, EXP_MANTENER },
  { {  20,  5, 120, 30, 60,  20, 10, 30 }, 500, -1, EXP_MANTENER },
  { { -20, -5,  20, 10, 30, 120, 30, 60 }, 500, -1, EXP_MANTENER },
  { {   0, 10, 150, 60, 20, 150, 60, 20 }, 700, -1, EXP_MANTENER },
  { {   0,  0,   0,  0,  0,   0,  0,  0 }, 900, -1, EXP_NORMAL },
};

static const PasoGesto G_FELIZ[] = {
  { { 0, 10, 140, 45, 20, 140, 45, 20 }, 800, -1, EXP_FELIZ },
  { { M,  M, 120, 35, 50, 120, 35, 50 }, 400, -1, EXP_MANTENER },
  { { M,  M, 140, 45, 20, 140, 45, 20 }, 400, -1, EXP_MANTENER },
  { { 0,  0,   0,  0,  0,   0,  0,  0 }, 1000, -1, EXP_NORMAL },
};

#if CON_PATAS
#define SIN_CAMBIO { M, M, M, M, M, M, M, M }

static const PasoGesto G_AGACHARSE[] = {
  { { 0, 0, 0, 0, 0, 0, 0, 0 },     1200, 40, EXP_NORMAL, true, { P_DE_PIE } },
  { { 0, 8, 40, 20, 30, 40, 20, 30 }, 2500, 30, EXP_SORPRESA, true, { P_AGACHADA } },
  { SIN_CAMBIO,                      800, -1, EXP_MANTENER },
  { { 0, 0, 0, 0, 0, 0, 0, 0 },     2500, 30, EXP_NORMAL, true, { P_DE_PIE } },
};

static const PasoGesto G_BALANCEAR[] = {
  { SIN_CAMBIO, 1200, 40, EXP_FELIZ,    true, { P_DE_PIE } },
  { { -15, M, M, 15, M, M, 15, M }, 1300, 20, EXP_MANTENER, true, { P_PESO_DER } },
  { {  15, M, M, 15, M, M, 15, M }, 2000, 20, EXP_MANTENER, true, { P_PESO_IZQ } },
  { { -15, M, M, 15, M, M, 15, M }, 2000, 20, EXP_MANTENER, true, { P_PESO_DER } },
  { {   0, M, M,  0, M, M,  0, M }, 1300, 20, EXP_NORMAL,   true, { P_DE_PIE } },
};

// Caminata estatica: el centro de masa siempre queda sobre el pie de apoyo.
// Es un punto de partida: hay que ajustar angulos y tiempos con el robot real.
static const PasoGesto G_CAMINAR[] = {
  { SIN_CAMBIO, 1200, 30, EXP_NORMAL,   true, { P_DE_PIE } },
  { SIN_CAMBIO, 1500, 20, EXP_MANTENER, true, { P_PESO_DER } },
  { SIN_CAMBIO, 1000, 40, EXP_MANTENER, true, { -10, 15, 30, 15, 10,   10, 50, 80, 30, -10 } },
  { SIN_CAMBIO, 1000, 40, EXP_MANTENER, true, { -10, 15, 30, 15, 10,   10, 35, 45, 10, -10 } },
  { SIN_CAMBIO, 1500, 20, EXP_MANTENER, true, {   0,  5, 30, 25,  0,    0, 25, 30,  5,   0 } },
  { SIN_CAMBIO, 1500, 20, EXP_MANTENER, true, {  10,  5, 30, 25, -10, -10, 25, 30,  5,  10 } },
  { SIN_CAMBIO, 1000, 40, EXP_MANTENER, true, {  10, 50, 80, 30, -10, -10, 25, 30,  5,  10 } },
  { SIN_CAMBIO, 1000, 40, EXP_MANTENER, true, {  10, 35, 45, 10, -10, -10, 25, 30,  5,  10 } },
  { SIN_CAMBIO, 1500, 20, EXP_MANTENER, true, {   0, 25, 30,  5,  0,    0,  5, 30, 25,   0 } },
  { SIN_CAMBIO, 1500, 20, EXP_NORMAL,   true, { P_DE_PIE } },
};
#undef SIN_CAMBIO
#endif

#undef M

#define N_PASOS(a) (uint8_t)(sizeof(a) / sizeof((a)[0]))

static const Gesto GESTOS[] = {
  { "saludar", G_SALUDAR, N_PASOS(G_SALUDAR) },
  { "asentir", G_ASENTIR, N_PASOS(G_ASENTIR) },
  { "negar",   G_NEGAR,   N_PASOS(G_NEGAR) },
  { "abrazar", G_ABRAZAR, N_PASOS(G_ABRAZAR) },
  { "pensar",  G_PENSAR,  N_PASOS(G_PENSAR) },
  { "bailar",  G_BAILAR,  N_PASOS(G_BAILAR) },
  { "feliz",   G_FELIZ,   N_PASOS(G_FELIZ) },
#if CON_PATAS
  { "agacharse", G_AGACHARSE, N_PASOS(G_AGACHARSE) },
  { "balancear", G_BALANCEAR, N_PASOS(G_BALANCEAR) },
  { "caminar",   G_CAMINAR,   N_PASOS(G_CAMINAR) },
#endif
};

inline const Pose *buscarPose(const char *n) {
  for (const Pose &p : POSES) if (strcasecmp(p.nombre, n) == 0) return &p;
  return nullptr;
}

inline const Gesto *buscarGesto(const char *n) {
  for (const Gesto &g : GESTOS) if (strcasecmp(g.nombre, n) == 0) return &g;
  return nullptr;
}

inline Expresion expresionPorNombre(const char *n) {
  if (strcasecmp(n, "feliz") == 0) return EXP_FELIZ;
  if (strcasecmp(n, "sorpresa") == 0) return EXP_SORPRESA;
  if (strcasecmp(n, "dormida") == 0) return EXP_DORMIDA;
  return EXP_NORMAL;
}
