// Modelos y alojamientos de servos. Todo en el marco local del servo:
// eje en el origen, cara superior del cuerpo en z=0, salida hacia +Z,
// cuerpo hacia +X a partir de -S_SOFF.

function servo_centro(s) = s[S_L] / 2 - s[S_SOFF];

module servo_modelo(s) {
    c = servo_centro(s);
    color(C_SERVO) {
        translate([-s[S_SOFF], -s[S_W] / 2, -s[S_H]]) cube([s[S_L], s[S_W], s[S_H]]);
        translate([c - s[S_TABL] / 2, -s[S_W] / 2, -s[S_TABD] - s[S_TABT]])
            cube([s[S_TABL], s[S_W], s[S_TABT]]);
    }
    color("#d8d8d8") {
        cylinder(d = s[S_SD], h = s[S_SH]);
        translate([0, 0, s[S_HORNH] - 2.5]) cylinder(d = s[S_HORND], h = 2.5);
    }
}

// Ventana por donde asoma el cuerpo, desde el plano de las orejas hasta la cara exterior.
module servo_ventana(s, afuera = 2) {
    translate([-s[S_SOFF] - CLR, -s[S_W] / 2 - CLR, -s[S_TABD] - 0.01])
        cube([s[S_L] + 2 * CLR, s[S_W] + 2 * CLR, s[S_TABD] + afuera]);
}

// Camino de insercion: huella de las orejas desde su plano hasta "prof" bajo la cara superior.
module servo_cavidad(s, prof) {
    c = servo_centro(s);
    translate([c - s[S_TABL] / 2 - CLR, -s[S_W] / 2 - CLR, -prof])
        cube([s[S_TABL] + 2 * CLR, s[S_W] + 2 * CLR, prof - s[S_TABD]]);
}

module servo_tornillos(s, d = undef) {
    c = servo_centro(s);
    ys = s[S_HDY] > 0 ? [-s[S_HDY] / 2, s[S_HDY] / 2] : [0];
    for (sx = [-1, 1], y = ys)
        translate([c + sx * s[S_HDX] / 2, y, -s[S_TABD] - 1])
            cylinder(d = d == undef ? s[S_PILOT] : d, h = s[S_TABD] + 3);
}

// Bloque solido alrededor de la ventana donde apoyan las orejas.
module servo_pad(s, margen = 4, extra = 4) {
    c = servo_centro(s);
    translate([c - s[S_TABL] / 2 - margen, -s[S_W] / 2 - margen, -s[S_TABD]])
        cube([s[S_TABL] + 2 * margen, s[S_W] + 2 * margen, s[S_TABD] + extra]);
}

// Agujeros para atornillar la bocina. Marco: z=0 en la cara de contacto,
// la pieza queda en z>0. "acceso" deja pasar el destornillador del tornillo central.
module bocina_agujeros(s, prof = 8, d = HORN_SCREW_PILOT, acceso = 7.5, largo_acceso = 200) {
    n = s[S_HORNN];
    a0 = n == 4 ? 45 : 90;
    for (i = [0 : n - 1])
        rotate(a0 + i * 360 / n) translate([s[S_HORNR], 0, -0.01]) cylinder(d = d, h = prof);
    if (acceso > 0) translate([0, 0, -0.01]) cylinder(d = acceso, h = largo_acceso);
}
