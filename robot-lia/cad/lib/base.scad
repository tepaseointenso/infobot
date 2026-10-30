// Base: aloja ESP32, PCA9685, fuente, parlante e interruptor.
// Se imprime con la cubierta superior sobre la cama (sin soportes).

function base_cols() = [for (a = [45 : 90 : 315]) [BASE_COL_R * cos(a), BASE_COL_R * sin(a)]];

module base_exterior() {
    hull() {
        cylinder(r = BASE_R, h = BASE_H - BASE_CH);
        cylinder(r = BASE_R - BASE_CH, h = BASE_H);
    }
}

module base_interior() {
    hull() {
        translate([0, 0, -1]) cylinder(r = BASE_R - WALL, h = BASE_H - BASE_CH + 1 - BASE_DECK + WALL);
        translate([0, 0, BASE_H - BASE_DECK - 0.01]) cylinder(r = BASE_R - BASE_CH - WALL * 0.6, h = 0.01);
    }
}

module base_rejilla(r = 18, d = 3.2, paso = 5.2) {
    for (i = [-6 : 6], j = [-6 : 6]) {
        x = i * paso + (j % 2 == 0 ? 0 : paso / 2);
        z = j * paso * 0.866;
        if (sqrt(x * x + z * z) <= r) translate([x, 0, z]) cyl_y(d, -WALL - 4, 4);
    }
}

module base() {
    difference() {
        union() {
            difference() { base_exterior(); base_interior(); }
            intersection() {
                base_exterior();
                for (p = base_cols()) translate([p.x, p.y, BASE_PLATE_T]) cylinder(d = 11, h = BASE_H);
            }
        }
        // tornillos M3 hacia los bujes del torso
        for (p = TORSO_BOSS) translate([p.x, p.y, BASE_H - 10]) cylinder(d = 3.4, h = 20);
        // paso de cables hacia el torso
        rbox_z([-35, -20, BASE_H - 10], [35, 20, BASE_H + 1], 6);
        // pilotos de la tapa inferior
        for (p = base_cols()) translate([p.x, p.y, BASE_PLATE_T - 1]) cylinder(d = 2.6, h = 14);
        // rejilla del parlante (frente)
        translate([0, -BASE_R + WALL / 2, 40]) base_rejilla();
        // trasera: jack DC 5.5x2.1, interruptor redondo 12 mm, USB del ESP32
        translate([-30, BASE_R - 6, 32]) cyl_y(8.2, 0, 12);
        translate([0, BASE_R - 6, 32]) cyl_y(12.4, 0, 12);
        translate([30, BASE_R - 6, 14]) rbox_y([-6.5, 0, -4], [6.5, 12, 4], 2);
    }
}

module base_tapa() {
    r = BASE_R - WALL - 0.4;
    difference() {
        union() {
            cylinder(r = r, h = BASE_PLATE_T);
            // PCA9685 (Adafruit 16 canales): agujeros 55.9 x 19.0
            for (x = [-55.9 / 2, 55.9 / 2], y = [-19 / 2, 19 / 2])
                translate([x, y - 20, 0]) cylinder(d = 6.5, h = BASE_PLATE_T + 6);
            // bordes guia para el ESP32 DevKit (55 x 28)
            for (x = [30 - 15.5, 30 + 15.5]) translate([x - 1, 22, 0]) cube([2, 52, BASE_PLATE_T + 4]);
        }
        for (p = base_cols()) {
            translate([p.x, p.y, -1]) cylinder(d = 3.4, h = 10);
            translate([p.x, p.y, -0.01]) cylinder(d1 = 6.8, d2 = 3.4, h = 1.8);
        }
        for (x = [-55.9 / 2, 55.9 / 2], y = [-19 / 2, 19 / 2])
            translate([x, y - 20, 1]) cylinder(d = 2.2, h = 20);
        // ranuras para bridas (ESP32, regulador, fuente)
        for (p = [[30, 30], [30, 66], [-40, 40], [-40, 62], [0, -62], [-30, -62], [30, -62]])
            for (dx = [-8, 8]) translate([p.x + dx - 1.5, p.y - 2, -1]) cube([3, 4, 10]);
        // ventilacion
        for (i = [-2 : 2]) translate([-62, i * 10 - 2, -1]) rbox_z([0, 0, 0], [22, 4, 10], 2);
        // patas de goma 12 mm
        for (a = [0 : 90 : 270]) rotate(a) translate([70, 0, -0.01]) cylinder(d = 12.5, h = 1);
    }
}
