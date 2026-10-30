// ============================================================================
//  Robot "Lia" - parametros globales (mm)
//  Mide tus servos y bocinas con un calibre y ajusta estos valores antes de
//  imprimir: el diseño completo se recalcula a partir de aqui.
// ============================================================================

$fn = 64;

WALL = 2.4;        // espesor de pared
CLR  = 0.3;        // holgura por lado para piezas que encajan

// ---------------------------------------------------------------------------
//  Servos. Marco local: eje del servo en el origen, plano superior del cuerpo
//  en z=0, eje de salida hacia +Z, largo del cuerpo hacia +X.
// ---------------------------------------------------------------------------
S_L     = 0;   // largo del cuerpo
S_W     = 1;   // ancho del cuerpo
S_H     = 2;   // alto del cuerpo (sin eje)
S_TABL  = 3;   // largo total incluyendo orejas de montaje
S_TABT  = 4;   // espesor de las orejas
S_TABD  = 5;   // distancia de la cara superior del cuerpo a la cara superior de las orejas
S_HDX   = 6;   // distancia entre agujeros de las orejas (a lo largo)
S_HDY   = 7;   // distancia entre agujeros de las orejas (a lo ancho, 0 = un agujero)
S_HD    = 8;   // diametro del agujero de oreja
S_SOFF  = 9;   // distancia del eje al extremo cercano del cuerpo
S_SH    = 10;  // altura del eje sobre el cuerpo
S_SD    = 11;  // diametro del eje / estria
S_HORNH = 12;  // distancia de la cara superior del cuerpo a la cara exterior de la bocina
S_HORND = 13;  // diametro de la bocina redonda
S_HORNR = 14;  // radio del circulo de agujeros de la bocina
S_HORNN = 15;  // cantidad de agujeros usados de la bocina
S_PILOT = 16;  // agujero piloto para los tornillos de las orejas (autorroscantes)

//             L     W     H    tabL  tabT  tabD  hdx   hdy  hd   soff  sh   sd   hornH hornD hornR n  pilot
MG996R = [ 40.7, 19.7, 36.9, 54.0, 2.5,  8.2, 49.5, 10.0, 4.3, 10.2, 6.0, 5.8,  9.0, 24.0,  8.0, 4, 2.6 ];
MG90S  = [ 22.8, 12.2, 22.5, 32.5, 2.5,  4.1, 27.8,  0.0, 2.2,  6.0, 5.5, 4.8,  7.0, 20.0,  7.0, 2, 1.6 ];
DS3218 = [ 40.0, 20.0, 37.2, 54.5, 2.5,  8.8, 49.5, 10.0, 4.3, 10.0, 4.0, 5.9,  8.0, 24.0,  8.0, 4, 2.6 ];

HORN_SCREW_PILOT = 1.8;  // tornillo M2 autorroscante de bocina
HORN_SCREW_PASS  = 2.4;  // paso libre para M2

// "patas" = humanoide con piernas; "escritorio" = torso sobre la base cilindrica
VARIANTE = "patas";

// ---------------------------------------------------------------------------
//  Base (electronica, solo variante escritorio)
// ---------------------------------------------------------------------------
BASE_R   = 95;
BASE_H   = 80;
BASE_CH  = 8;      // chaflan superior
BASE_DECK = 3;     // espesor de la cubierta superior
BASE_COL_R = 86;   // radio de las columnas de la tapa inferior
BASE_PLATE_T = 3;

// ---------------------------------------------------------------------------
//  Torso
// ---------------------------------------------------------------------------
TORSO_W  = 150;    // pecho
TORSO_D  = 110;
TORSO_R  = 28;     // radio de esquinas en planta
TORSO_CH = 20;     // chaflan superior a 45 grados (se imprime boca abajo sin soportes)
Z_T0     = BASE_H;
Z_T      = 240;    // cara superior del torso
Z_S      = Z_T - 42;   // altura del eje de los hombros
TORSO_BOSS = [[52, 42], [-52, 42], [52, -42], [-52, -42]];
// Cintura, solo variante patas: el pecho sigue en TORSO_W y la base se estrecha.
// La cadera vuelve a abrirse debajo. Asi la silueta se lee como la de una mujer.
WAIST_W   = 138;
WAIST_R   = 26;
Z_CINTURA = Z_T0 + 54;

// ---------------------------------------------------------------------------
//  Cuello (pan + tilt)
// ---------------------------------------------------------------------------
NECK_W  = 50;
NECK_D  = 32;
NECK_H  = 66;
NECK_AX = NECK_H / 2 + (MG996R[S_L] / 2 - MG996R[S_SOFF]);  // altura del eje tilt
CAP_T   = 6;
Z_NECK  = Z_T + MG996R[S_HORNH];
Z_TILT  = Z_NECK + NECK_AX;

// ---------------------------------------------------------------------------
//  Cabeza (coordenadas relativas al eje tilt)
// ---------------------------------------------------------------------------
HEAD_W  = 150;
HEAD_H  = 135;
HEAD_R  = 45;
HEAD_Y0 = -60;            // cara frontal
HEAD_YB = 60 - HEAD_R;    // centro de las esferas traseras (parte trasera en y=+60)
HEAD_Z0 = -30;            // piso de la cabeza
FACE_T  = 14;             // profundidad de la pieza "cara"
Y_CUT   = HEAD_Y0 + FACE_T;

EYE_DX  = 33;
EYE_Z   = 45;
EYE_WIN = 33.0;           // ventana visible de la pantalla GC9A01 de 1.28"
EYE_PCB = 39.5;           // diametro del modulo (medir el tuyo)

EAR_T   = 5;
EAR_XP  = NECK_W / 2 + MG996R[S_HORNH];   // cara interior de la oreja del lado de la bocina
EAR_XN  = -(NECK_W / 2 + CAP_T + 1);      // cara interior de la oreja del lado del eje loco
EAR_Z0  = -14;
DECK_Z  = 30;
DECK_T  = 3;

MAG_D   = 6.3;            // imanes de neodimio 6x3 mm
MAG_H   = 3.2;
MAG_POS = [[52, -8], [-52, -8], [52, 83], [-52, 83]];   // (x, z)

// ---------------------------------------------------------------------------
//  Brazos
// ---------------------------------------------------------------------------
LINK_L  = 28;             // hombro (eslabon con servo de abduccion)
LINK_D  = 32;
LINK_Z0 = -37.5;
LINK_Z1 = 14;
ROLL_Z  = -12;

UA_W    = 28;             // brazo superior
UA_D    = 24;
UA_LEN  = 70;             // eje hombro -> eje codo

FA_T    = 16;             // antebrazo + mano
FA_W    = 22;
FA_LEN  = 62;

// ---------------------------------------------------------------------------
//  Piernas (5 servos DS3218 por pierna)
//  Cada servo va en un "bloque" con tapa trasera; la pieza siguiente lo abraza
//  con una horquilla: oreja atornillada a la bocina y oreja con eje loco M4.
// ---------------------------------------------------------------------------
SP      = DS3218;
BLK_M   = 5;              // pared alrededor de las orejas del servo
BLK_W   = 46;             // profundidad del bloque (cara del eje -> cara de la tapa)
BLK_D   = SP[S_W] + 2 * (CLR + 6.7);
BLK_CAP = 6;
BLK_C   = -(SP[S_L] / 2 - SP[S_SOFF]);
BLK_Z0  = BLK_C - SP[S_TABL] / 2 - CLR - BLK_M;
BLK_Z1  = BLK_C + SP[S_TABL] / 2 + CLR + BLK_M;
BLK_R   = norm([BLK_Z0, BLK_D / 2]);           // radio barrido por el bloque al girar
BLK_LIBRE = BLK_R + 1.5;
X_S     = (BLK_W + BLK_CAP + 1 - SP[S_HORNH]) / 2;   // cara del eje: horquilla centrada
EAR_IN  = X_S + SP[S_HORNH];                  // cara interior de cada oreja de horquilla
EAR_R   = 18;                                 // radio del disco de las orejas

LEG_X   = 45;             // separacion de cada pierna respecto al centro
PELV_T  = 8;              // espesor de la placa pelvis
// Cadera: cresta a los costados, mas ancha que la cintura. 212 mm entra
// en una cama de 220 mm sin brim. Poco espesor en Y: de perfil no hay bulbo.
HIP_W       = 212;        // ancho de la cresta, de bulbo a bulbo
// Hueco por el que se mueve el mecanismo de cada pierna. Tiene que quedar
// dentro de la piel (HIP_W) y fuera del barrido del servo de roll.
HUECO_W = 102;
HUECO_D = 100;
HUECO_Y = -4;
HUECO_Z0 = 0;
HUECO_Z1 = 70;   // el roll a +15 grados sube el bloque hasta z~70
PELV_LIBRE = 29;          // eje roll de cadera -> cara inferior de la pelvis
Z_HIP   = Z_T0 - PELV_T - PELV_LIBRE;
CAD_DZ  = BLK_D / 2 + EAR_R + 1;             // roll de cadera -> pitch de cadera
CAD_YF  = BLK_Z1 - BLK_W;                     // cara del eje del servo roll de cadera
SEG_L   = 75;             // muslo y tibia (eje a eje)
TOB_DZ  = BLK_LIBRE + BLK_D / 2 + 2;          // pitch de tobillo -> roll de tobillo
TOB_YF  = -(BLK_W + BLK_CAP) / 2;
SOLE_Z  = 32.5;           // roll de tobillo -> cara superior de la suela
SOLE_T  = 6;
FOOT_X  = 40;             // medio ancho del pie
FOOT_Y0 = -75;            // punta
FOOT_Y1 = 55;             // talon
Z_SUELO = Z_HIP - CAD_DZ - 2 * SEG_L - TOB_DZ - SOLE_Z - SOLE_T;

// ---------------------------------------------------------------------------
//  Colores del ensamble
// ---------------------------------------------------------------------------
C_SHELL  = "#f4f1ea";
C_ACCENT = "#8cc5d1";
C_FACE   = "#2b2d31";
C_SERVO  = "#3a3a3a";
