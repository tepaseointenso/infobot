#!/usr/bin/env bash
# Verifica que no haya choques entre piezas y servos en los rangos de movimiento
# que usa el firmware. Cada caso debe dar "OK (vacio)".
set -uo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
SCAD="$RAIZ/herramientas/interferencias.scad"
TMP="$(mktemp -d)"
fallas=0

chequear() {
  local caso="$1" ang="$2" brazo="${3:-0,0,0}"
  local log="$TMP/$caso.log"
  openscad -D "caso=\"$caso\"" -D "ang=$ang" -D "brazo=[$brazo]" -o "$TMP/$caso.stl" "$SCAD" >"$log" 2>&1
  # "Volumes: 1" = solo el volumen exterior de CGAL, es decir, contacto sin volumen
  if grep -q "Current top level object is empty" "$log" || grep -Eq "Volumes: +1$" "$log"; then
    printf "%-16s ang=%5s brazo=[%s]  OK (vacio)\n" "$caso" "$ang" "$brazo"
  else
    printf "%-16s ang=%5s brazo=[%s]  CHOQUE\n" "$caso" "$ang" "$brazo"
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

rm -rf "$TMP"
echo "Choques: $fallas"
exit $fallas
