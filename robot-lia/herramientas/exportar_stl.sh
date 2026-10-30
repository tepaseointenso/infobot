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
  PIEZAS=(base base_tapa torso cuello cuello_tapa cabeza cara
          hombro_der hombro_izq brazo_der brazo_izq antebrazo_der antebrazo_izq)
fi

for p in "${PIEZAS[@]}"; do
  echo ">> $p"
  openscad --hardwarnings -D "parte=\"$p\"" -o "$SALIDA/$p.stl" "$CAD"
  python3 "$RAIZ/herramientas/stl_binario.py" "$SALIDA/$p.stl"
done
