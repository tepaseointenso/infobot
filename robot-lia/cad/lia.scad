// ============================================================================
//  Robot companera "Lia" - archivo principal
//
//  Abrir en OpenSCAD y elegir "parte". Cada pieza sale ya orientada para
//  imprimir. Desde la terminal:
//      openscad -D 'parte="torso"' -o ../stl/torso.stl lia.scad
//  o simplemente:  ../herramientas/exportar_stl.sh
// ============================================================================

include <config.scad>
include <lib/formas.scad>
include <lib/servos.scad>
include <lib/base.scad>
include <lib/torso.scad>
include <lib/cuello.scad>
include <lib/cabeza.scad>
include <lib/brazos.scad>

// [ensamble, base, base_tapa, torso, cuello, cuello_tapa, cabeza, cara,
//  hombro_der, hombro_izq, brazo_der, brazo_izq, antebrazo_der, antebrazo_izq]
parte = "ensamble";

// Pose del ensamble (grados)
pan = 0;
tilt = 0;
der = [0, 0, 0];   // [flexion, abduccion, codo] brazo derecho
izq = [0, 0, 0];
mostrar_servos = true;

module ensamble() {
    color(C_SHELL) base();
    color(C_ACCENT) base_tapa();
    color(C_SHELL) torso();
    if (mostrar_servos) { en_pan() servo_modelo(MG996R); en_hombros() servo_modelo(MG996R); }

    translate([0, 0, Z_NECK]) rotate([0, 0, pan]) {
        color(C_ACCENT) cuello();
        color(C_ACCENT) cuello_tapa();
        if (mostrar_servos) en_tilt() servo_modelo(MG996R);
        translate([0, 0, NECK_AX]) rotate([-tilt, 0, 0]) {
            color(C_SHELL) cabeza();
            color(C_SHELL) cara();
            ojos_modelo();
        }
    }

    // Lia mira hacia -Y, asi que su brazo izquierdo esta en +X
    brazo_ensamble(izq[0], izq[1], izq[2], mostrar_servos);
    mirror([1, 0, 0]) brazo_ensamble(der[0], der[1], der[2], mostrar_servos);
}

// Orientaciones de impresion (cara indicada apoyada en la cama, z=0)
module imprimir(p) {
    if (p == "base")            rotate([180, 0, 0]) translate([0, 0, -BASE_H]) base();
    else if (p == "base_tapa")  base_tapa();
    else if (p == "torso")      rotate([180, 0, 0]) translate([0, 0, -Z_T]) torso();
    else if (p == "cuello")     translate([0, 0, NECK_W / 2]) rotate([0, -90, 0]) cuello();
    else if (p == "cuello_tapa") translate([0, 0, NECK_W / 2 + CAP_T]) rotate([0, -90, 0]) cuello_tapa();
    else if (p == "cabeza")     translate([0, 0, -Y_CUT]) rotate([90, 0, 0]) cabeza();
    else if (p == "cara")       translate([0, 0, -HEAD_Y0]) rotate([90, 0, 0]) cara();
    else if (p == "hombro_izq") translate([0, 0, LINK_D / 2]) rotate([-90, 0, 0]) hombro();
    else if (p == "brazo_izq")  translate([0, 0, UA_W / 2]) rotate([0, -90, 0]) brazo();
    else if (p == "antebrazo_izq") rotate([0, -90, 0]) antebrazo();
    else if (p == "hombro_der")    mirror([1, 0, 0]) imprimir("hombro_izq");
    else if (p == "brazo_der")     mirror([1, 0, 0]) imprimir("brazo_izq");
    else if (p == "antebrazo_der") mirror([1, 0, 0]) imprimir("antebrazo_izq");
    else echo(str("Parte desconocida: ", p));
}

// Todas las piezas en orientacion de impresion, para revisar de un vistazo.
module piezas() {
    lista = [["base", [-110, 120], C_SHELL], ["base_tapa", [110, 120], C_ACCENT],
             ["torso", [-110, -40], C_SHELL], ["cabeza", [80, -30], C_SHELL],
             ["cara", [260, -30], C_SHELL], ["cuello", [-250, -40], C_ACCENT],
             ["cuello_tapa", [-250, 40], C_ACCENT],
             ["hombro_der", [-40, -170], C_ACCENT], ["hombro_izq", [0, -170], C_ACCENT],
             ["brazo_der", [70, -170], C_SHELL], ["brazo_izq", [70, -210], C_SHELL],
             ["antebrazo_der", [180, -170], C_ACCENT], ["antebrazo_izq", [180, -210], C_ACCENT]];
    for (p = lista) translate([p[1][0], p[1][1], 0]) color(p[2]) imprimir(p[0]);
}

if (parte == "ensamble") ensamble();
else if (parte == "piezas") piezas();
else imprimir(parte);
