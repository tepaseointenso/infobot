#!/usr/bin/env python3
"""Convierte STL ASCII (lo que exporta OpenSCAD 2021) a STL binario, en el mismo archivo."""
import struct
import sys


def convertir(ruta):
    with open(ruta, "rb") as f:
        datos = f.read()
    if not datos.lstrip().startswith(b"solid") or b"facet" not in datos[:2000]:
        return False
    triangulos = []
    normal, vertices = None, []
    for linea in datos.decode("ascii", "replace").splitlines():
        p = linea.split()
        if not p:
            continue
        if p[0] == "facet":
            normal = tuple(map(float, p[2:5]))
            vertices = []
        elif p[0] == "vertex":
            vertices.append(tuple(map(float, p[1:4])))
        elif p[0] == "endfacet":
            triangulos.append((normal, vertices))
    with open(ruta, "wb") as f:
        f.write(b"Robot Lia - OpenSCAD".ljust(80, b" "))
        f.write(struct.pack("<I", len(triangulos)))
        for n, v in triangulos:
            f.write(struct.pack("<12fH", *n, *v[0], *v[1], *v[2], 0))
    return True


if __name__ == "__main__":
    for ruta in sys.argv[1:]:
        if convertir(ruta):
            print(f"binario: {ruta}")
