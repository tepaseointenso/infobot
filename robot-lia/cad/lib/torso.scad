// Torso: carcasa con el servo pan del cuello arriba y los dos servos de hombro
// a los costados. Se imprime boca abajo (cara superior en la cama).

module en_pan() translate([0, 0, Z_T]) children();
module en_hombro_der() translate([TORSO_W / 2, 0, Z_S]) rotate([0, 90, 0]) children();
module en_hombros() {
    en_hombro_der() children();
    mirror([1, 0, 0]) en_hombro_der() children();
}

module torso_exterior() {
    hull() {
        rbox_z([-TORSO_W / 2, -TORSO_D / 2, Z_T0], [TORSO_W / 2, TORSO_D / 2, Z_T - TORSO_CH], TORSO_R);
        rbox_z([-TORSO_W / 2 + TORSO_CH, -TORSO_D / 2 + TORSO_CH, Z_T - 0.01],
               [TORSO_W / 2 - TORSO_CH, TORSO_D / 2 - TORSO_CH, Z_T], max(TORSO_R - TORSO_CH, 1));
    }
}

module torso_interior() {
    t = WALL;
    ins = TORSO_CH - 0.586 * t;
    hull() {
        rbox_z([-TORSO_W / 2 + t, -TORSO_D / 2 + t, Z_T0 - 1],
               [TORSO_W / 2 - t, TORSO_D / 2 - t, Z_T - TORSO_CH - 0.414 * t], TORSO_R - t);
        rbox_z([-TORSO_W / 2 + t + ins, -TORSO_D / 2 + t + ins, Z_T - t - 0.01],
               [TORSO_W / 2 - t - ins, TORSO_D / 2 - t - ins, Z_T - t], max(TORSO_R - t - ins, 1));
    }
}

// Pad del hombro con chaflan a 45 grados hacia +Z global (cara que mira a la cama al imprimir).
module pad_hombro(s = MG996R, m = 4) {
    c = servo_centro(s);
    x0 = c - s[S_TABL] / 2 - m;
    hull() {
        servo_pad(s, m, 6);
        translate([x0 - s[S_TABD], -s[S_W] / 2 - m, -1.5]) cube([s[S_TABL] + 2 * m + s[S_TABD], s[S_W] + 2 * m, 7.5]);
    }
}

module torso_bujes() {
    for (p = TORSO_BOSS) {
        sx = sign(p.x); sy = sign(p.y);
        cx = sx * (TORSO_W / 2 - TORSO_R); cy = sy * (TORSO_D / 2 - TORSO_R);
        k = (TORSO_R - WALL / 2) / norm([p.x - cx, p.y - cy]);
        hull() {
            translate([p.x, p.y, Z_T0]) cylinder(d = 10, h = 12);
            translate([cx + (p.x - cx) * k, cy + (p.y - cy) * k, Z_T0 + 30]) cylinder(d = 3, h = 1);
        }
    }
}

module torso() {
    difference() {
        union() {
            difference() { torso_exterior(); torso_interior(); }
            intersection() {
                torso_exterior();
                union() {
                    en_pan() servo_pad(MG996R, 4, 4);
                    en_hombros() pad_hombro();
                    torso_bujes();
                }
            }
        }
        en_pan() { servo_ventana(MG996R); servo_cavidad(MG996R, 60); servo_tornillos(MG996R); }
        en_hombros() { servo_ventana(MG996R); servo_cavidad(MG996R, 60); servo_tornillos(MG996R); }
        for (p = TORSO_BOSS) translate([p.x, p.y, Z_T0 - 1]) cylinder(d = 2.6, h = 14);
        // paso de cables: cabeza (arriba) y brazos (costados, detras del hombro)
        translate([-36, 22, Z_T - 10]) cylinder(d = 18, h = 20);
        for (sx = [-1, 1]) translate([sx * TORSO_W / 2, 40, Z_S - 10]) rotate([0, 90, 0]) cylinder(d = 14, h = 20, center = true);
        // version con patas: interruptor y salida del cable de carga de la bateria
        if (VARIANTE == "patas") {
            translate([-30, TORSO_D / 2 - 6, Z_T0 + 30]) cyl_y(12.4, 0, 12);
            translate([25, TORSO_D / 2 - 6, Z_T0 + 30]) rbox_y([-15, 0, -7], [15, 12, 7], 3);
        }
    }
}
