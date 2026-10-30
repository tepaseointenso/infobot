// Brazo del lado +X, que es el izquierdo de Lia (mira hacia -Y); el derecho es su espejo. Cadena:
//   torso --MG996R (flexion hombro)--> hombro --MG90S (abduccion)--> brazo
//         --MG90S (codo)--> antebrazo con mano
// Cada pieza en el marco de la cara de su bocina de entrada.

module en_abduccion() translate([LINK_L / 2, -LINK_D / 2, ROLL_Z]) rotate([90, 0, 0]) rotate([0, 0, -90]) children();
module en_codo() translate([UA_W / 2, -UA_D / 2, -UA_LEN]) rotate([0, 90, 0]) rotate([0, 0, 180]) children();

module hombro() {
    difference() {
        rbox_y([0, -LINK_D / 2, LINK_Z0], [LINK_L, LINK_D / 2, LINK_Z1], 6);
        en_abduccion() { servo_ventana(MG90S); servo_cavidad(MG90S, LINK_D + 5); servo_tornillos(MG90S); }
        rotate([0, 90, 0]) bocina_agujeros(MG996R, prof = 7);
    }
}

module brazo() {
    difference() {
        hull() {
            rbox_x([-UA_W / 2, -UA_D, -14], [UA_W / 2, 0, 12], 8);
            rbox_x([-UA_W / 2 + 1, -UA_D, -UA_LEN - 15], [UA_W / 2, 0, -UA_LEN + 25], 6);
        }
        en_codo() { servo_ventana(MG90S); servo_cavidad(MG90S, UA_W + 5); servo_tornillos(MG90S); }
        rotate([90, 0, 0]) bocina_agujeros(MG90S, prof = 7, acceso = 6);
        // canal para el cable del codo por la cara interior
        translate([-UA_W / 2 - 1, -UA_D / 2 - 3, -UA_LEN + 20]) cube([3, 6, UA_LEN]);
    }
}

module antebrazo() {
    difference() {
        hull() {
            rbox_x([0, -FA_W / 2, -22], [FA_T, FA_W / 2, 11], 6);
            translate([0, 0, -FA_LEN + 13]) cyl_x(26, 0, FA_T);
        }
        rotate([0, 90, 0]) bocina_agujeros(MG90S, prof = 7, acceso = 6);
    }
}

// Ensamble del brazo del lado +X con angulos en grados:
// flexion > 0 = brazo hacia adelante, abduccion > 0 = hacia afuera, codo > 0 = flexiona.
module brazo_ensamble(flexion = 0, abduccion = 0, codo = 0, servos = true) {
    translate([TORSO_W / 2 + MG996R[S_HORNH], 0, Z_S]) rotate([-flexion, 0, 0]) {
        color(C_ACCENT) hombro();
        if (servos) en_abduccion() servo_modelo(MG90S);
        translate([LINK_L / 2, -LINK_D / 2 - MG90S[S_HORNH], ROLL_Z]) rotate([0, -abduccion, 0]) {
            color(C_SHELL) brazo();
            if (servos) en_codo() servo_modelo(MG90S);
            translate([UA_W / 2 + MG90S[S_HORNH], -UA_D / 2, -UA_LEN]) rotate([-codo, 0, 0])
                color(C_ACCENT) antebrazo();
        }
    }
}
