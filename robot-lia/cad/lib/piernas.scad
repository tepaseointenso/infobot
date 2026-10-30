// Piernas: 5 grados de libertad por pierna con servos DS3218.
//   pelvis --roll cadera--> cadera --pitch cadera--> muslo --rodilla--> tibia
//          --pitch tobillo--> tobillo --roll tobillo--> pie
// Muslo y tibia son la misma pieza ("pierna").
//
// Bloque de servo: marco con el eje del servo en el origen sobre la cara +X,
// cuerpo hacia -X y largo del servo hacia -Z. El servo entra por la cara -X,
// que se cierra con "tapa_servo"; la tapa lleva el eje loco M4 de la horquilla.
// Horquilla: marco con el eje de giro sobre X; oreja con la bocina en +X.
// Todo lo de este archivo es la pierna del lado +X (izquierda de Lia).

M_ROLL = [[0, 0, -1, 0], [-1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 0, 1]];  // eje -> -Y, largo -> +X

function blk_tornillos() = [for (y = [-1, 1] * (BLK_D / 2 - 3.35), z = [BLK_Z0 + 2.5, BLK_Z1 - 2.5]) [y, z]];

module blk_servo() rotate([0, 90, 0]) children();

module bloque_pos() rbox_x([-BLK_W, -BLK_D / 2, BLK_Z0], [0, BLK_D / 2, BLK_Z1], 4);

module bloque_neg() {
    blk_servo() { servo_ventana(SP); servo_cavidad(SP, BLK_W + 5); servo_tornillos(SP); }
    for (p = blk_tornillos()) translate([-BLK_W, p[0], p[1]]) cyl_x(2.6, -1, 10);
}

module bloque_servo_modelo() {
    blk_servo() servo_modelo(SP);
    color(C_ACCENT) tapa_servo();
}

module tapa_servo() {
    x0 = -BLK_W - BLK_CAP;
    difference() {
        rbox_x([x0, -BLK_D / 2, BLK_Z0], [-BLK_W, BLK_D / 2, BLK_Z1], 4);
        for (p = blk_tornillos()) translate([0, p[0], p[1]]) {
            cyl_x(3.4, x0 - 1, -BLK_W + 1);
            cyl_x(6.2, x0 - 1, x0 + 3);
        }
        cyl_x(4.3, x0 - 1, 0);
        translate([-BLK_W - 3.4, 0, 0]) rotate([0, 90, 0]) cylinder(d = 8.3, h = 4, $fn = 6);
        translate([x0 - 1, -5, BLK_Z0 + 5.5]) cube([BLK_CAP + 2, 10, 8]);
    }
}

module oreja_pos(x0, x1, hasta) {
    hull() {
        cyl_x(2 * EAR_R, x0, x1);
        translate([x0, -BLK_D / 2, min(hasta, 0)]) cube([x1 - x0, BLK_D, abs(hasta)]);
    }
}

// hasta: z hasta donde llegan las orejas para unirse al resto de la pieza
module horquilla_pos(hasta) {
    oreja_pos(EAR_IN, EAR_IN + EAR_T, hasta);
    oreja_pos(-EAR_IN - EAR_T, -EAR_IN, hasta);
}

module horquilla_neg() {
    translate([EAR_IN, 0, 0]) rotate([0, 90, 0])
        bocina_agujeros(SP, prof = EAR_T + 2, d = HORN_SCREW_PASS, acceso = 8, largo_acceso = EAR_T + 2);
    cyl_x(4.4, -EAR_IN - EAR_T - 1, -EAR_IN + 1);
}

// Horquilla para un eje roll (sobre Y) cuyo servo tiene la cara del eje en y = yf mirando a -Y.
module en_horquilla_roll(yf) translate([0, yf + X_S, 0]) rotate([0, 0, -90]) children();

// Travesaño entre las orejas, con chaflan a 45 grados hacia el bloque inferior.
module travesano(z_arriba, z_abajo, x_bloque0, x_bloque1) {
    hull() {
        translate([-EAR_IN - EAR_T, -BLK_D / 2, z_abajo]) cube([2 * (EAR_IN + EAR_T), BLK_D, z_arriba - z_abajo]);
        translate([x_bloque0, -BLK_D / 2, z_abajo - (EAR_IN + EAR_T - min(-x_bloque0, x_bloque1))])
            cube([x_bloque1 - x_bloque0, BLK_D, 0.01]);
    }
}

// ---------------------------------------------------------------------------
//  Cadera (marco: eje roll de cadera)
// ---------------------------------------------------------------------------
module cad_roll_blk() translate([0, CAD_YF, 0]) multmatrix(M_ROLL) children();
module cad_pitch_blk() translate([X_S, 0, -CAD_DZ]) rotate([-90, 0, 0]) children();

module cadera() {
    difference() {
        union() {
            cad_roll_blk() bloque_pos();
            cad_pitch_blk() bloque_pos();
            translate([max(-BLK_Z1, X_S - BLK_W), CAD_YF, -CAD_DZ + BLK_D / 2 - 0.5])
                cube([X_S - max(-BLK_Z1, X_S - BLK_W), BLK_Z1 - CAD_YF, CAD_DZ - BLK_D + 1]);
        }
        cad_roll_blk() bloque_neg();
        cad_pitch_blk() bloque_neg();
    }
}

// ---------------------------------------------------------------------------
//  Muslo / tibia (marco: eje superior)
// ---------------------------------------------------------------------------
module seg_blk() translate([X_S, 0, -SEG_L]) children();

module pierna() {
    z_blk = -SEG_L + BLK_Z1;
    difference() {
        union() {
            horquilla_pos(-BLK_LIBRE - 1);
            // sin chaflan: debajo estan la tapa del bloque y la horquilla siguiente
            translate([-EAR_IN - EAR_T, -BLK_D / 2, z_blk - 0.5])
                cube([2 * (EAR_IN + EAR_T), BLK_D, -BLK_LIBRE - z_blk + 0.5]);
            seg_blk() bloque_pos();
        }
        horquilla_neg();
        seg_blk() bloque_neg();
    }
}

// ---------------------------------------------------------------------------
//  Tobillo (marco: eje pitch de tobillo)
// ---------------------------------------------------------------------------
module tob_roll_blk() translate([0, TOB_YF, -TOB_DZ]) multmatrix(M_ROLL) children();

module tobillo() {
    z_blk = -TOB_DZ + BLK_D / 2;
    difference() {
        union() {
            horquilla_pos(-BLK_LIBRE - 1);
            travesano(-BLK_LIBRE, z_blk - 0.5, -BLK_Z1, EAR_IN + EAR_T);
            tob_roll_blk() bloque_pos();
        }
        horquilla_neg();
        tob_roll_blk() bloque_neg();
    }
}

// ---------------------------------------------------------------------------
//  Pie (marco: eje roll de tobillo)
// ---------------------------------------------------------------------------
module pie() {
    difference() {
        union() {
            en_horquilla_roll(TOB_YF) horquilla_pos(-SOLE_Z - 0.5);
            rbox_z([-FOOT_X, FOOT_Y0, -SOLE_Z - SOLE_T], [FOOT_X, FOOT_Y1, -SOLE_Z], 14);
        }
        en_horquilla_roll(TOB_YF) horquilla_neg();
        // alivianado y agarre: ranuras en la suela
        for (y = [FOOT_Y0 + 18 : 14 : FOOT_Y1 - 18])
            if (y < -45 || y > 38) translate([0, y, -SOLE_Z - SOLE_T - 1]) rbox_z([-FOOT_X + 10, -3, 0], [FOOT_X - 10, 3, 2.5], 3);
    }
}

// ---------------------------------------------------------------------------
//  Pelvis (coordenadas globales): tapa inferior del torso, electronica arriba,
//  horquillas de roll abajo y la cadera ancha de la silueta.
// ---------------------------------------------------------------------------
module en_caderas() {
    translate([LEG_X, 0, Z_HIP]) children();
    mirror([1, 0, 0]) translate([LEG_X, 0, Z_HIP]) children();
}

// Cadera ancha, como un short: nace en la cintura, se abre a los lados
// y los muslos salen por debajo. El hueco de cada pierna la recorta.
module caderas_forma() {
    hull() {
        translate([0, -6, 74]) cube([WAIST_W - 4, 82, 14], center = true);
        translate([0, -8, 46]) cube([HIP_W - 4, 96, 32], center = true);
    }
}

// Hueco de una pierna. Arriba es un poco mas chico para no romper la piel.
module hueco_pierna(sgn = 1) {
    hull() {
        translate([sgn * LEG_X, HUECO_Y, HUECO_Z0]) cube([HUECO_W, HUECO_D, 1], center = true);
        // Arriba mas angosto que la base, pero con aire para el roll a -15°,
        // que roza la arista interna de la punta.
        translate([sgn * LEG_X, HUECO_Y, HUECO_Z1]) cube([HUECO_W - 14, HUECO_D - 14, 1], center = true);
    }
}

module pelvis() {
    z0 = Z_T0 - PELV_T;
    difference() {
        union() {
            rbox_z([-WAIST_W / 2, -TORSO_D / 2, z0], [WAIST_W / 2, TORSO_D / 2, Z_T0], WAIST_R);
            caderas_forma();
        }
        // tornillos M3 hacia los bujes del torso, cabeza por debajo
        for (p = TORSO_BOSS) {
            translate([p.x, p.y, z0 - 1]) {
                cylinder(d = 3.4, h = PELV_T + 2);
                cylinder(d = 6.5, h = 4);
            }
            translate([p.x, p.y, 24]) cylinder(d = 8, h = z0 - 24);
        }
        // paso de cables de cada pierna, abierto a traves de la cadera
        for (sx = [-1, 1]) {
            translate([sx * LEG_X, 0, z0 - 1]) rbox_z([-11, -8, 0], [11, 8, PELV_T + 2], 4);
            translate([sx * LEG_X, 0, 24]) rbox_z([-11, -8, 0], [11, 8, z0 - 22], 4);
        }
        // 2 x PCA9685 apilados (separadores M2.5 de nylon)
        for (x = [-55.9 / 2, 55.9 / 2], y = [-19 / 2, 19 / 2]) translate([x, y - 30, z0 + 2]) cylinder(d = 2.2, h = PELV_T);
        // bridas: ESP32 (centro), regulador (lado derecho), bateria 2S (atras)
        for (p = [[-20, -16], [20, -16], [-20, 16], [20, 16], [58, -12], [58, -32], [-58, -12], [-58, -32]])
            translate([p.x - 2, p.y - 1.5, 24]) cube([4, 3, z0 - 22]);
        for (sx = [-1, 1]) translate([sx * 53 - 1.5, 22, 24]) cube([3, 13, z0 - 22]);
        hueco_pierna(1);
        hueco_pierna(-1);
    }
    // Despues del hueco: si no, el vaciado se lleva las orejas del roll.
    en_caderas() en_horquilla_roll(CAD_YF) difference() {
        horquilla_pos(PELV_LIBRE + 1);
        horquilla_neg();
    }
}

// ---------------------------------------------------------------------------
//  Ensamble de la pierna del lado +X. Angulos en grados:
//  cr  roll de cadera   (+ = el pie se aleja del centro)
//  cp  pitch de cadera  (+ = muslo hacia adelante)
//  ro  rodilla          (+ = flexiona, tibia hacia atras)
//  tp  pitch de tobillo (+ = punta del pie hacia arriba)
//  tr  roll de tobillo  (mismo sentido que cr; pie plano si tr = -cr)
//  Pie plano en el plano sagital si tp = ro - cp.
// ---------------------------------------------------------------------------
module pierna_ensamble(cr = 0, cp = 0, ro = 0, tp = 0, tr = 0, servos = true) {
    translate([LEG_X, 0, Z_HIP]) rotate([0, -cr, 0]) {
        color(C_UNION) cadera();
        if (servos) { cad_roll_blk() bloque_servo_modelo(); cad_pitch_blk() bloque_servo_modelo(); }
        translate([0, 0, -CAD_DZ]) rotate([-cp, 0, 0]) {
            color(C_MUSLO) pierna();
            if (servos) seg_blk() bloque_servo_modelo();
            translate([0, 0, -SEG_L]) rotate([ro, 0, 0]) {
                color(C_CANILLA) pierna();
                if (servos) seg_blk() bloque_servo_modelo();
                translate([0, 0, -SEG_L]) rotate([-tp, 0, 0]) {
                    color(C_BOTA) tobillo();
                    if (servos) tob_roll_blk() bloque_servo_modelo();
                    translate([0, 0, -TOB_DZ]) rotate([0, -tr, 0]) color(C_MUSLO) pie();
                }
            }
        }
    }
}
