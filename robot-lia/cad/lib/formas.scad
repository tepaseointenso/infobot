// Formas basicas redondeadas. a = esquina minima [x,y,z], b = esquina maxima.

module rrect(w, h, r) {
    rr = max(min(r, w / 2 - 0.01, h / 2 - 0.01), 0.01);
    offset(r = rr) square([w - 2 * rr, h - 2 * rr], center = true);
}

// Aristas verticales redondeadas (prisma a lo largo de Z).
module rbox_z(a, b, r) {
    translate([(a.x + b.x) / 2, (a.y + b.y) / 2, a.z])
        linear_extrude(b.z - a.z) rrect(b.x - a.x, b.y - a.y, r);
}

// Aristas paralelas a X redondeadas (prisma a lo largo de X).
module rbox_x(a, b, r) {
    translate([a.x, (a.y + b.y) / 2, (a.z + b.z) / 2])
        rotate([0, 90, 0]) linear_extrude(b.x - a.x) rrect(b.z - a.z, b.y - a.y, r);
}

// Aristas paralelas a Y redondeadas (prisma a lo largo de Y).
module rbox_y(a, b, r) {
    translate([(a.x + b.x) / 2, a.y, (a.z + b.z) / 2])
        rotate([-90, 0, 0]) linear_extrude(b.y - a.y) rrect(b.x - a.x, b.z - a.z, r);
}

module cyl_x(d, x0, x1) { translate([x0, 0, 0]) rotate([0, 90, 0]) cylinder(d = d, h = x1 - x0); }
module cyl_y(d, y0, y1) { translate([0, y0, 0]) rotate([-90, 0, 0]) cylinder(d = d, h = y1 - y0); }
