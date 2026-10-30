// Cabeza en dos piezas: "cabeza" (trasera, con orejas del tilt y cubierta interna)
// y "cara" (frente con dos pantallas redondas GC9A01 como ojos). Se unen con imanes.
// Marco: origen en el eje tilt.

function cab_esquinas(r) = [for (sx = [-1, 1], sz = [0, 1])
    [sx * (HEAD_W / 2 - HEAD_R), sz == 0 ? HEAD_Z0 + HEAD_R : HEAD_Z0 + HEAD_H - HEAD_R]];

module cabeza_forma(t = 0) {
    r = HEAD_R - t;
    hull() {
        rbox_y([-HEAD_W / 2 + t, HEAD_Y0 + 2 + t, HEAD_Z0 + t], [HEAD_W / 2 - t, HEAD_YB, HEAD_Z0 + HEAD_H - t], r);
        if (t == 0)
            rbox_y([-HEAD_W / 2 + 2, HEAD_Y0, HEAD_Z0 + 2], [HEAD_W / 2 - 2, HEAD_Y0 + 2, HEAD_Z0 + HEAD_H - 2], r - 2);
        for (p = cab_esquinas()) translate([p.x, HEAD_YB, p.y]) sphere(r = r);
    }
}

module cabeza_carcasa() difference() { cabeza_forma(0); cabeza_forma(WALL); }

module solo_y(y0, y1) intersection() { children(); translate([-500, y0, -500]) cube([1000, y1 - y0, 1000]); }

// centro de la esquina redondeada mas cercana a un punto (x, z)
function cab_centro(p) = [sign(p.x) * (HEAD_W / 2 - HEAD_R),
                          p.y < HEAD_Z0 + HEAD_H / 2 ? HEAD_Z0 + HEAD_R : HEAD_Z0 + HEAD_H - HEAD_R];

module iman_buje(p, y0, y1, nervio = false) {
    translate([p.x, 0, p.y]) cyl_y(10, y0, y1);
    if (nervio) {
        c = cab_centro(p);
        k = (HEAD_R - WALL / 2) / norm(p - c);
        hull() {
            translate([p.x, 0, p.y]) cyl_y(4, y0, y1);
            translate([c.x + (p.x - c.x) * k, 0, c.y + (p.y - c.y) * k]) cyl_y(3, y0, y1);
        }
    }
}

module cabeza() {
    difference() {
        union() {
            solo_y(Y_CUT, 100) cabeza_carcasa();
            intersection() {
                cabeza_forma(0.5);
                union() {
                    // orejas del eje tilt
                    translate([EAR_XP, Y_CUT, EAR_Z0]) cube([EAR_T, 16 - Y_CUT, DECK_Z + DECK_T - EAR_Z0]);
                    translate([EAR_XN - EAR_T, Y_CUT, EAR_Z0]) cube([EAR_T, 16 - Y_CUT, DECK_Z + DECK_T - EAR_Z0]);
                    // cubierta interna (rigidiza y sostiene las orejas)
                    translate([-200, Y_CUT, DECK_Z]) cube([400, 200, DECK_T]);
                    for (p = MAG_POS) iman_buje(p, Y_CUT, Y_CUT + 12, true);
                }
            }
        }
        // abertura del piso para el cuello
        rbox_z([-36, -27, HEAD_Z0 - 1], [36, 27, HEAD_Z0 + WALL + 1], 8);
        // oreja de la bocina: tornillos M2 hacia la bocina + acceso al tornillo central
        translate([EAR_XP, 0, 0]) rotate([0, 90, 0])
            bocina_agujeros(MG996R, prof = EAR_T + 2, d = HORN_SCREW_PASS, acceso = 8, largo_acceso = EAR_T + 2);
        // oreja del eje loco M4
        cyl_x(4.4, EAR_XN - EAR_T - 1, EAR_XN + 1);
        // pasos de cables en la cubierta
        for (sx = [-1, 1]) translate([sx * 50, 10, DECK_Z - 1]) cylinder(d = 16, h = DECK_T + 2);
        translate([0, Y_CUT + 7, DECK_Z - 1]) rbox_z([-25, -4, 0], [25, 4, DECK_T + 2], 3);
        for (p = MAG_POS) translate([p.x, 0, p.y]) cyl_y(MAG_D, Y_CUT - 1, Y_CUT + MAG_H);
        // ventilacion / salida de sonido trasera
        for (i = [-2 : 2]) translate([i * 9, 60, HEAD_Z0 + HEAD_H / 2]) rbox_y([-2, -10, -15], [2, 1, 15], 2);
    }
}

module cara() {
    difference() {
        union() {
            solo_y(-100, Y_CUT) cabeza_carcasa();
            // labio de centrado que entra en la cabeza
            difference() {
                union() {
                    solo_y(Y_CUT - 1.5, Y_CUT + 3) cabeza_forma(WALL + CLR);
                    solo_y(Y_CUT - 1.5, Y_CUT) cabeza_forma(WALL - 0.5);
                }
                cabeza_forma(WALL + CLR + 1.6);
                translate([-200, Y_CUT - 2, DECK_Z - 1]) cube([400, 10, DECK_T + 2]);
                for (p = MAG_POS) translate([p.x, Y_CUT, p.y]) cube([28, 20, 28], center = true);
            }
            for (p = MAG_POS) iman_buje(p, HEAD_Y0 + 1, Y_CUT);
            // portaojos
            for (sx = [-1, 1]) translate([sx * EYE_DX, 0, EYE_Z]) difference() {
                cyl_y(EYE_PCB + 4, HEAD_Y0 + 1, HEAD_Y0 + WALL + 5);
                cyl_y(EYE_PCB, HEAD_Y0 + WALL, HEAD_Y0 + WALL + 6);
                translate([-6, HEAD_Y0, -EYE_PCB / 2 - 4]) cube([12, 20, 8]);
            }
        }
        for (sx = [-1, 1]) translate([sx * EYE_DX, 0, EYE_Z]) {
            cyl_y(EYE_WIN, HEAD_Y0 - 1, HEAD_Y0 + WALL + 1);
            translate([0, HEAD_Y0 - 0.01, 0]) rotate([-90, 0, 0]) cylinder(d1 = EYE_WIN + 3, d2 = EYE_WIN, h = 1.5);
        }
        for (p = MAG_POS) translate([p.x, 0, p.y]) cyl_y(MAG_D, Y_CUT - MAG_H, Y_CUT + 1);
        // microfono
        translate([0, 0, EYE_Z - 42]) cyl_y(2.5, HEAD_Y0 - 1, HEAD_Y0 + WALL + 1);
    }
}

module ojos_modelo() {
    for (sx = [-1, 1]) translate([sx * EYE_DX, HEAD_Y0 + WALL + 0.2, EYE_Z]) {
        color("#111") cyl_y(EYE_PCB - 0.5, 0, 3.5);
        color("#7fd6ff") translate([0, -0.3, 0]) cyl_y(EYE_WIN - 1, 0, 0.3);
        color("#0b1d2a") translate([0, -0.5, -2]) cyl_y(14, 0, 0.3);
        color("white") translate([4, -0.7, 4]) cyl_y(4, 0, 0.3);
    }
}
