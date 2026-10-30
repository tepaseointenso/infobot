#!/usr/bin/env bash
# Verifica que no haya choques entre piezas y servos en los rangos de movimiento
# que usa el firmware. Cada caso debe dar "OK (vacio)".
set -uo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
SCAD="$RAIZ/herramientas/interferencias.scad"
TMP="$(mktemp -d)"
fallas=0

chequear() {
  local caso="$1" ang="$2" brazo="${3:-0,0,0}" pi="${4:-0,0,0,0,0}" pd="${5:-0,0,0,0,0}"
  local log="$TMP/$caso.log"
  openscad -D "caso=\"$caso\"" -D "ang=$ang" -D "brazo=[$brazo]" -D "pi=[$pi]" -D "pd=[$pd]" \
    -o "$TMP/$caso.stl" "$SCAD" >"$log" 2>&1
  # "Volumes: 1" = solo el volumen exterior de CGAL, es decir, contacto sin volumen
  if grep -q "Current top level object is empty" "$log" || grep -Eq "Volumes: +1$" "$log"; then
    printf "%-17s ang=%4s brazo=[%s] piernas=[%s] [%s]  OK (vacio)\n" "$caso" "$ang" "$brazo" "$pi" "$pd"
  else
    printf "%-17s ang=%4s brazo=[%s] piernas=[%s] [%s]  CHOQUE\n" "$caso" "$ang" "$brazo" "$pi" "$pd"
    fallas=$((fallas + 1))
  fi
}

chequear torso_servos 0
chequear cuello_servo 0
chequear hombro_servo 0
chequear brazo_servo 0
for a in -15 0 15; do chequear cabeza_cuello "$a"; chequear cabeza_torso "$a"; done
for a in -30 0 90 160; do chequear brazo_torso "$a"; done
for a in 0 90; do chequear brazo_interno "$a"; done
for a in 0 60 110; do chequear antebrazo_brazo "$a"; done
# brazos levantados (gestos saludar, bailar, pensar) con la cabeza girando
chequear brazo_cabeza 10 150,25,60
chequear brazo_cabeza 20 120,30,60
chequear brazo_cabeza -20 120,30,60
chequear brazo_cabeza 70 150,0,0
chequear brazo_cabeza -70 155,0,110

# piernas: servos en sus bloques y cada articulacion en todo su rango de firmware
chequear piernas_servos 0
for a in -15 0 15; do chequear cadera_pelvis "$a"; done
for a in -30 0 45 90; do chequear muslo_cadera "$a"; done
for a in 0 60 110; do chequear tibia_muslo "$a"; done
for a in -30 0 30; do chequear tobillo_tibia "$a"; done
for a in -20 0 20; do chequear pie_tobillo "$a"; done
# una pierna contra la otra: de pie, agachada, paso y peso cargado a un lado
chequear piernas_entre_si 0 0,0,0 0,15,30,15,0 0,15,30,15,0
chequear piernas_entre_si 0 0,0,0 0,60,110,50,0 0,60,110,50,0
chequear piernas_entre_si 0 0,0,0 0,40,70,30,0 0,-20,0,20,0
chequear piernas_entre_si 0 0,0,0 -10,15,30,15,10 10,15,30,15,-10
chequear piernas_entre_si 0 0,0,0 -15,40,70,30,15 15,-20,0,20,-15
chequear pierna_cuerpo 0 0,0,0 15,90,110,0,0

rm -rf "$TMP"
echo "Choques: $fallas"
exit $fallas
