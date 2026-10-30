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
include <lib/piernas.scad>

// ensamble | piezas | una pieza:
//   torso, cuello, cuello_tapa, cabeza, cara, hombro_*, brazo_*, antebrazo_*
//   con patas:  pelvis, cadera_*, pierna_* (x2 por lado), tobillo_*, pie_*, tapa_servo (x10)
//   escritorio: base, base_tapa
//   (* = izq o der)
parte = "ensamble";

// Pose del ensamble (grados)
pan = 0;
tilt = 0;
der = [0, 0, 0];   // [flexion, abduccion, codo] brazo derecho
izq = [0, 0, 0];
// [roll cadera, pitch cadera, rodilla, pitch tobillo, roll tobillo]
pierna_der = [0, 0, 0, 0, 0];
pierna_izq = [0, 0, 0, 0, 0];
mostrar_servos = true;

module ensamble() {
    if (VARIANTE == "patas") {
        translate([0, 0, -Z_SUELO]) {
            cuerpo_superior();
            color(C_SHELL) pelvis();
            pierna_ensamble(pierna_izq[0], pierna_izq[1], pierna_izq[2], pierna_izq[3], pierna_izq[4], mostrar_servos);
            mirror([1, 0, 0]) pierna_ensamble(pierna_der[0], pierna_der[1], pierna_der[2], pierna_der[3], pierna_der[4], mostrar_servos);
        }
    } else {
        color(C_SHELL) base();
        color(C_ACCENT) base_tapa();
        cuerpo_superior();
    }
}

module cuerpo_superior() {
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
    else if (p == "pelvis")     rotate([180, 0, 0]) translate([0, 0, -Z_T0]) pelvis();
    else if (p == "cadera_izq") translate([0, 0, BLK_Z1]) rotate([-90, 0, 0]) cadera();
    else if (p == "pierna_izq") translate([0, 0, SEG_L - BLK_Z0]) pierna();
    else if (p == "tobillo_izq") translate([0, 0, TOB_DZ + BLK_D / 2]) tobillo();
    else if (p == "pie_izq")    translate([0, 0, SOLE_Z + SOLE_T]) pie();
    else if (p == "tapa_servo") translate([0, 0, BLK_W + BLK_CAP]) rotate([0, -90, 0]) tapa_servo();
    else if (p == "cadera_der")  mirror([1, 0, 0]) imprimir("cadera_izq");
    else if (p == "pierna_der")  mirror([1, 0, 0]) imprimir("pierna_izq");
    else if (p == "tobillo_der") mirror([1, 0, 0]) imprimir("tobillo_izq");
    else if (p == "pie_der")     mirror([1, 0, 0]) imprimir("pie_izq");
    else echo(str("Parte desconocida: ", p));
}

// Todas las piezas en orientacion de impresion, para revisar de un vistazo.
module piezas() {
    inferior = VARIANTE == "patas"
        ? [["pelvis", [-150, 150], C_SHELL], ["cadera_der", [40, 150], C_ACCENT], ["cadera_izq", [130, 150], C_ACCENT],
           ["pierna_der", [230, 140], C_SHELL], ["pierna_der", [230, 60], C_SHELL],
           ["pierna_izq", [320, 140], C_SHELL], ["pierna_izq", [320, 60], C_SHELL],
           ["tobillo_der", [60, 220], C_ACCENT], ["tobillo_izq", [150, 220], C_ACCENT],
           ["pie_der", [250, 260], C_SHELL], ["pie_izq", [340, 260], C_SHELL],
           ["tapa_servo", [-200, 140], C_ACCENT]]
        : [["base", [-110, 120], C_SHELL], ["base_tapa", [110, 120], C_ACCENT]];
    for (p = inferior) translate([p[1][0], p[1][1], 0]) color(p[2]) imprimir(p[0]);
    lista = [["torso", [-110, -40], C_SHELL], ["cabeza", [80, -30], C_SHELL],
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
