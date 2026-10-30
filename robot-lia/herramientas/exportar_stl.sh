#!/usr/bin/env bash
# Exporta todas las piezas a robot-lia/stl/ (ya orientadas para imprimir).
# Uso: herramientas/exportar_stl.sh [pieza ...]
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
CAD="$RAIZ/cad/lia.scad"
SALIDA="$RAIZ/stl"
mkdir -p "$SALIDA"

PIEZAS=("$@")
if [ ${#PIEZAS[@]} -eq 0 ]; then
  PIEZAS=(torso cuello cuello_tapa cabeza cara gorra
          hombro_der hombro_izq brazo_der brazo_izq antebrazo_der antebrazo_izq
          pelvis cadera_der cadera_izq pierna_der pierna_izq tobillo_der tobillo_izq
          pie_der pie_izq tapa_servo
          base base_tapa)
fi

exportar() {  # pieza archivo [variante]
  echo ">> $2"
  openscad --hardwarnings -D "parte=\"$1\"" -D "VARIANTE=\"${3:-patas}\"" -o "$SALIDA/$2.stl" "$CAD"
  python3 "$RAIZ/herramientas/stl_binario.py" "$SALIDA/$2.stl"
}

for p in "${PIEZAS[@]}"; do
  exportar "$p" "$p"
done
# el torso de escritorio no lleva el interruptor ni la salida de carga traseros
if [ $# -eq 0 ]; then exportar torso torso_escritorio escritorio; fi
