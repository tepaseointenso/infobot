// Cuello: bloque que gira con el servo pan y aloja el servo tilt.
// Marco: origen en la cara exterior de la bocina pan (centro del eje).

module en_tilt() translate([NECK_W / 2, 0, NECK_AX]) rotate([0, 90, 0]) children();

function tapa_tornillos() = [for (y = [-12.8, 12.8], z = [3, NECK_H - 3]) [y, z]];

module cuello() {
    difference() {
        rbox_x([-NECK_W / 2, -NECK_D / 2, 0], [NECK_W / 2, NECK_D / 2, NECK_H], 4);
        en_tilt() { servo_ventana(MG996R); servo_cavidad(MG996R, NECK_W + 5); servo_tornillos(MG996R); }
        bocina_agujeros(MG996R, prof = 5);
        for (p = tapa_tornillos()) translate([-NECK_W / 2, p[0], p[1]]) cyl_x(2.6, -1, 10);
    }
}

module cuello_tapa() {
    x0 = -NECK_W / 2 - CAP_T;
    difference() {
        rbox_x([x0, -NECK_D / 2, 0], [-NECK_W / 2, NECK_D / 2, NECK_H], 4);
        for (p = tapa_tornillos()) translate([0, p[0], p[1]]) {
            cyl_x(3.4, x0 - 1, 0);
            cyl_x(6.2, x0 - 1, x0 + 3);
        }
        // eje loco M4: tornillo fijo a la tapa con tuerca atrapada por dentro
        translate([0, 0, NECK_AX]) {
            cyl_x(4.3, x0 - 1, 0);
            translate([-NECK_W / 2 - 3.4, 0, 0]) rotate([0, 90, 0]) cylinder(d = 8.3, h = 4, $fn = 6);
        }
        // salida del cable del servo tilt
        translate([x0 - 1, -5, 7]) cube([CAP_T + 2, 10, 7]);
    }
}
