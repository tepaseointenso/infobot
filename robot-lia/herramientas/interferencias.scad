// Chequeo de interferencias: cada caso debe exportar una geometria vacia.
// Uso: herramientas/chequear_interferencias.sh
include <../cad/config.scad>
include <../cad/lib/formas.scad>
include <../cad/lib/servos.scad>
include <../cad/lib/base.scad>
include <../cad/lib/torso.scad>
include <../cad/lib/cuello.scad>
include <../cad/lib/cabeza.scad>
include <../cad/lib/brazos.scad>

caso = "torso_servos";
ang = 0;
brazo = [0, 0, 0];

// La bocina del tilt queda en contacto con la oreja (es donde se atornilla),
// por eso aqui se usa solo el cuerpo del servo.
module cuello_conjunto() {
    cuello(); cuello_tapa(); en_tilt() servo_cuerpo(MG996R);
}

module servo_cuerpo(s) {
    c = servo_centro(s);
    translate([-s[S_SOFF], -s[S_W] / 2, -s[S_H]]) cube([s[S_L], s[S_W], s[S_H]]);
    translate([c - s[S_TABL] / 2, -s[S_W] / 2, -s[S_TABD] - s[S_TABT]]) cube([s[S_TABL], s[S_W], s[S_TABT]]);
}

if (caso == "torso_servos")
    intersection() { torso(); union() { en_pan() servo_cuerpo(MG996R); en_hombros() servo_cuerpo(MG996R); } }
else if (caso == "cuello_servo")
    intersection() { union() { cuello(); cuello_tapa(); } en_tilt() servo_cuerpo(MG996R); }
else if (caso == "hombro_servo")
    intersection() { hombro(); en_abduccion() servo_cuerpo(MG90S); }
else if (caso == "brazo_servo")
    intersection() { brazo(); en_codo() servo_cuerpo(MG90S); }
else if (caso == "cabeza_cuello")
    // cabeza girando "ang" grados respecto del cuello (marco del eje tilt)
    intersection() {
        rotate([-ang, 0, 0]) union() { cabeza(); cara(); }
        translate([0, 0, -NECK_AX]) cuello_conjunto();
    }
else if (caso == "cabeza_torso")
    intersection() {
        translate([0, 0, Z_TILT]) rotate([-ang, 0, 0]) cabeza();
        torso();
    }
else if (caso == "brazo_torso")
    intersection() {
        brazo_ensamble(ang, 0, 0, false);
        union() { torso(); base(); }
    }
else if (caso == "brazo_interno")
    // brazo superior contra hombro, con abduccion "ang"
    intersection() {
        hombro();
        translate([LINK_L / 2, -LINK_D / 2 - MG90S[S_HORNH], ROLL_Z]) rotate([0, -ang, 0]) brazo();
    }
else if (caso == "brazo_cabeza")
    // brazo del lado +X en pose "brazo" = [flexion, abduccion, codo], cabeza girada "ang"
    intersection() {
        brazo_ensamble(brazo[0], brazo[1], brazo[2], false);
        translate([0, 0, Z_NECK]) rotate([0, 0, ang]) translate([0, 0, NECK_AX]) union() { cabeza(); cara(); }
    }
else if (caso == "antebrazo_brazo")
    intersection() {
        brazo();
        translate([UA_W / 2 + MG90S[S_HORNH], -UA_D / 2, -UA_LEN]) rotate([-ang, 0, 0]) antebrazo();
    }
