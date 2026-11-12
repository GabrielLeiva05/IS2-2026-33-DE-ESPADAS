package com.ejercicioIntegrador.dulcemayor.config;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Ilustraciones SVG para los productos de los datos de demostración. No hay fotos reales en el repositorio,
 * así que cada producto recibe un dibujo propio: la forma depende del tipo de producto (alfajor, tableta,
 * botella, lata, bolsa de snacks, frasco...) y el color, de su sabor (menta, frutilla, cola, dulce de leche...).
 * Resultado: el catálogo del sitio institucional y del panel de administración se ve variado sin depender
 * de archivos externos ni de conexión a internet.
 */
final class ImagenesDemo {

    private static final String[] MULTI = {"#f03e3e", "#fd7e14", "#fcc419", "#51cf66", "#339af0", "#cc5de8"};
    private static final String[] RESPALDO = {"#d6336c", "#7048e8", "#f08c00", "#1c7ed6", "#2f9e44", "#c2255c",
            "#0c8599", "#e8590c"};
    private static final String CHOCOLATE_LECHE = "#8d5524";
    private static final String CHOCOLATE_AMARGO = "#4a2c1a";
    private static final String MANI = "#d9a066";

    /** Palabra del nombre -> color. El orden importa: "chocolate" debe evaluarse antes que "cola". */
    private static final Map<String, String> SABORES = new LinkedHashMap<>();

    static {
        SABORES.put("semiamargo", CHOCOLATE_AMARGO);
        SABORES.put("brownie", CHOCOLATE_AMARGO);
        SABORES.put("cobertura", CHOCOLATE_AMARGO);
        SABORES.put("chocolate con leche", CHOCOLATE_LECHE);
        SABORES.put("chocolate", CHOCOLATE_LECHE);
        SABORES.put("cacao", "#6d4c41");
        SABORES.put("dulce de leche", "#c68642");
        SABORES.put("vainilla", "#f0d9a0");
        SABORES.put("menta", "#2fb68a");
        SABORES.put("frutilla", "#e64980");
        SABORES.put("lima", "#82c91e");
        SABORES.put("limonada", "#fcc419");
        SABORES.put("naranja", "#fd7e14");
        SABORES.put("pomelo", "#fa5252");
        SABORES.put("durazno", "#ffa94d");
        SABORES.put("membrillo", "#c0562b");
        SABORES.put("cola", "#5c3d2e");
        SABORES.put("energizante", "#e03131");
        SABORES.put("isotónica", "#15aabf");
        SABORES.put("agua mineral", "#a5d8ff");
        SABORES.put("listo para beber", "#ff922b");
        SABORES.put("ácidas", "#94d82d");
        SABORES.put("miel", "#e8a317");
        SABORES.put("turrón", "#e9c46a");
        SABORES.put("garrapiñada", "#b5651d");
        SABORES.put("pasta de maní", "#c98a4b");
        SABORES.put("papas", "#f4b400");
        SABORES.put("maní", "#d68c45");
        SABORES.put("palitos", "#f2a33a");
        SABORES.put("maíz", "#f0b429");
        SABORES.put("pochoclo", "#fa5252");
        SABORES.put("cereal", "#a9703a");
        SABORES.put("queso", "#f2b01e");
        SABORES.put("coco", "#c9b79c");
        SABORES.put("crackers", "#d9a65b");
        SABORES.put("grisines", "#d9a65b");
        SABORES.put("barquillos", "#d9a65b");
        SABORES.put("obleas", "#e64980");
        SABORES.put("chicles de menta", "#2fb68a");
        SABORES.put("frutales", "#e64980");
        SABORES.put("ositos", "#e64980");
        SABORES.put("aros", "#e64980");
        SABORES.put("pastillas", "#7048e8");
        SABORES.put("marshmallows", "#f783ac");
        SABORES.put("grajeas", "#cc5de8");
        SABORES.put("globos", "#f06595");
        SABORES.put("surtido", "#d6336c");
        SABORES.put("bombones", "#7a4a2e");
        SABORES.put("esencia", "#8d5524");
    }

    private ImagenesDemo() {
    }

    /** SVG cuadrado de 400x400 con el dibujo del producto y su nombre. */
    static byte[] svg(String nombre, String subcategoria, int indice) {
        return svg(nombre, subcategoria, indice, true);
    }

    /**
     * Igual que {@link #svg(String, String, int)}; con {@code conEtiqueta = false} devuelve solo el dibujo
     * (400x320, sin el nombre), útil como ilustración decorativa, por ejemplo en la portada del sitio.
     */
    static byte[] svg(String nombre, String subcategoria, int indice, boolean conEtiqueta) {
        String n = nombre.toLowerCase(Locale.ROOT);
        String color = colorBase(n, indice);
        int alto = conEtiqueta ? 400 : 320;
        StringBuilder s = new StringBuilder(4096);
        s.append(f("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"400\" height=\"%d\" viewBox=\"0 0 400 %d\" ", alto, alto))
                .append("role=\"img\"><title>").append(xml(nombre)).append("</title>");
        s.append(f("<rect width=\"400\" height=\"%d\" fill=\"%s\"/>", alto, mezclar(color, "#ffffff", 0.82)));
        s.append("<circle cx=\"200\" cy=\"175\" r=\"%s\" fill=\"#ffffff\" fill-opacity=\"0.55\"/>".formatted(conEtiqueta ? "158" : "148"));
        s.append("<ellipse cx=\"200\" cy=\"292\" rx=\"120\" ry=\"11\" fill=\"#000000\" fill-opacity=\"0.12\"/>");
        dibujar(s, n, subcategoria, color);
        if (conEtiqueta) {
            etiqueta(s, nombre);
        }
        return s.append("</svg>").toString().getBytes(StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------ Selección de la forma

    private static void dibujar(StringBuilder s, String n, String sub, String c) {
        switch (sub) {
            case "Alfajores" -> alfajores(s, n, c);
            case "Chocolates" -> chocolates(s, n, c);
            case "Caramelos" -> {
                if (n.contains("chupetines")) {
                    chupetines(s);
                } else {
                    caramelos(s, n.contains("frutales") || n.contains("rellenos"), c);
                }
            }
            case "Gomitas" -> {
                if (n.contains("aros")) {
                    aros(s);
                } else if (n.contains("pastillas")) {
                    tuboPastillas(s, c);
                } else if (n.contains("marshmallows")) {
                    malvaviscos(s);
                } else {
                    ositos(s, n.contains("ácidas"));
                }
            }
            case "Chicles" -> {
                if (n.contains("globos")) {
                    globos(s, c);
                } else {
                    chicles(s, c);
                }
            }
            case "Galletitas dulces" -> {
                if (n.contains("obleas")) {
                    oblea(s, c);
                } else if (n.contains("rellenas")) {
                    alfajor(s, 200, 160, 1.0, "#3b2a20", "#f8f9fa", false);
                    alfajor(s, 110, 245, 0.55, "#3b2a20", "#f8f9fa", false);
                    alfajor(s, 290, 245, 0.55, "#3b2a20", "#f8f9fa", false);
                } else if (n.contains("barquillos")) {
                    barquillos(s);
                } else {
                    galletas(s, n.contains("chips"));
                }
            }
            case "Galletitas saladas" -> {
                if (n.contains("grisines")) {
                    grisines(s);
                } else {
                    crackers(s, c);
                }
            }
            case "Snacks" -> bolsa(s, c, tipoSnack(n));
            case "Gaseosas" -> gaseosas(s, n, c);
            case "Jugos y aguas" -> {
                if (n.contains("en polvo")) {
                    sobres(s, c);
                } else if (n.contains("listo para beber")) {
                    briks(s, c);
                } else {
                    botellas(s, n.contains("pomelo") ? "#ff8787" : "#a5d8ff", "#339af0", "#1c7ed6", n.contains("1,5 l"));
                }
            }
            case "Energizantes e isotónicas" -> {
                if (n.contains("energizante")) {
                    latas(s, c);
                } else {
                    botellas(s, c, "#ffffff", "#0b7285", false);
                }
            }
            case "Barras y turrones" -> {
                if (n.contains("garrapiñada")) {
                    bolsa(s, c, "garrapinada");
                } else {
                    barras(s, n, c);
                }
            }
            case "Dulces y untables" -> {
                if (n.contains("membrillo")) {
                    panDulce(s, c);
                } else if (n.contains("cacao")) {
                    latasCacao(s, c);
                } else {
                    potes(s, c, n.contains("1 kg"));
                }
            }
            case "Repostería" -> {
                if (n.contains("premezcla")) {
                    cajasPremezcla(s, c, n.contains("brownie"));
                } else if (n.contains("cobertura")) {
                    tableta(s, CHOCOLATE_AMARGO, "#f08c00", 230);
                } else if (n.contains("grajeas")) {
                    frascoGrajeas(s, c);
                } else {
                    botella(s, 130, 215, 0.62, "#8d5524", "#ffffff", "#212529");
                    botella(s, 270, 215, 0.62, "#8d5524", "#ffffff", "#212529");
                    botella(s, 200, 195, 0.72, "#a8682f", "#ffffff", "#212529");
                }
            }
            default -> cajasPremezcla(s, c, false);
        }
    }

    private static String tipoSnack(String n) {
        if (n.contains("papas")) {
            return "papas";
        }
        if (n.contains("maní")) {
            return "mani";
        }
        if (n.contains("palitos")) {
            return "palitos";
        }
        if (n.contains("maíz")) {
            return "maiz";
        }
        return "pochoclo";
    }

    // ------------------------------------------------------------------ Color

    private static String colorBase(String n, int indice) {
        for (Map.Entry<String, String> e : SABORES.entrySet()) {
            if (n.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return RESPALDO[Math.floorMod(indice, RESPALDO.length)];
    }

    private static String oscuro(String hex) {
        return mezclar(hex, "#000000", 0.28);
    }

    private static String claro(String hex) {
        return mezclar(hex, "#ffffff", 0.45);
    }

    /** Mezcla dos colores #rrggbb: t = 0 devuelve a, t = 1 devuelve b. */
    private static String mezclar(String a, String b, double t) {
        int ra = Integer.parseInt(a.substring(1), 16);
        int rb = Integer.parseInt(b.substring(1), 16);
        int r = (int) Math.round(((ra >> 16) & 255) * (1 - t) + ((rb >> 16) & 255) * t);
        int g = (int) Math.round(((ra >> 8) & 255) * (1 - t) + ((rb >> 8) & 255) * t);
        int bl = (int) Math.round((ra & 255) * (1 - t) + (rb & 255) * t);
        return String.format(Locale.ROOT, "#%02x%02x%02x", r, g, bl);
    }

    // ------------------------------------------------------------------ Dulces y chocolates

    private static void alfajores(StringBuilder s, String n, String c) {
        if (n.contains("mini")) {
            alfajor(s, 200, 150, 0.62, c, "#f0c987", false);
            alfajor(s, 120, 225, 0.62, "#6b3f24", "#f0c987", false);
            alfajor(s, 280, 225, 0.62, "#e9ecef", "#f0c987", false);
        } else if (n.contains("maicena")) {
            alfajor(s, 200, 175, 1.0, "#f3e2b8", "#f0c987", false);
            s.append("<g fill=\"#ffffff\">");
            for (int i = 0; i < 9; i++) {
                s.append(f("<ellipse cx=\"%d\" cy=\"%d\" rx=\"7\" ry=\"3\" transform=\"rotate(%d %d %d)\"/>",
                        125 + i * 18, 160 + (i % 3) * 8, i * 25, 125 + i * 18, 160 + (i % 3) * 8));
            }
            s.append("</g>");
        } else if (n.contains("glaseado")) {
            alfajor(s, 200, 175, 1.0, "#f8f9fa", "#c68642", false);
        } else {
            alfajor(s, 200, 175, 1.0, c, n.contains("dulce de leche") ? "#f0c987" : "#f8f9fa", n.contains("triple"));
        }
    }

    /** Alfajor/sandwich visto de costado: base, relleno y tapa. */
    private static void alfajor(StringBuilder s, double cx, double cy, double k, String tapa, String relleno,
                                boolean triple) {
        s.append(f("<g transform=\"translate(%s %s) scale(%s)\">", num(cx), num(cy), num(k)));
        s.append(f("<ellipse cx=\"0\" cy=\"45\" rx=\"110\" ry=\"36\" fill=\"%s\"/>", oscuro(tapa)));
        s.append(f("<rect x=\"-110\" y=\"-10\" width=\"220\" height=\"55\" fill=\"%s\"/>", tapa));
        s.append(f("<rect x=\"-110\" y=\"8\" width=\"220\" height=\"18\" fill=\"%s\"/>", relleno));
        if (triple) {
            s.append(f("<rect x=\"-110\" y=\"-8\" width=\"220\" height=\"10\" fill=\"%s\"/>", mezclar(relleno, tapa, 0.5)));
        }
        s.append(f("<ellipse cx=\"0\" cy=\"-10\" rx=\"110\" ry=\"36\" fill=\"%s\"/>", mezclar(tapa, "#ffffff", 0.15)));
        s.append("<ellipse cx=\"-32\" cy=\"-18\" rx=\"52\" ry=\"10\" fill=\"#ffffff\" fill-opacity=\"0.28\"/></g>");
    }

    private static void chocolates(StringBuilder s, String n, String c) {
        if (n.contains("tableta")) {
            tableta(s, c, n.contains("semiamargo") ? "#c92a2a" : "#1c7ed6", 164);
        } else if (n.contains("bombones")) {
            bombones(s);
        } else if (n.contains("huevo")) {
            huevo(s, c);
        } else {
            barra(s, 200, 130, -6, c, "mani");
            barra(s, 200, 195, 4, mezclar(c, "#ffffff", 0.2), "mani");
            barra(s, 200, 258, -3, oscuro(c), "mani");
        }
    }

    private static void tableta(StringBuilder s, String choco, String envoltorio, int ancho) {
        int x = 200 - ancho / 2;
        int columnas = ancho / 54;
        s.append("<g transform=\"rotate(-8 200 175)\">");
        for (int fila = 0; fila < 2; fila++) {
            for (int col = 0; col < columnas; col++) {
                int px = x + 10 + col * (ancho - 20) / columnas;
                s.append(f("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"32\" rx=\"5\" fill=\"%s\"/>", px, 78 + fila * 36,
                        (ancho - 20) / columnas - 4, choco));
                s.append(f("<rect x=\"%d\" y=\"%d\" width=\"14\" height=\"5\" rx=\"2\" fill=\"#ffffff\" fill-opacity=\"0.25\"/>",
                        px + 5, 82 + fila * 36));
            }
        }
        s.append(f("<rect x=\"%d\" y=\"146\" width=\"%d\" height=\"18\" rx=\"4\" fill=\"#ced4da\"/>", x, ancho));
        s.append(f("<rect x=\"%d\" y=\"158\" width=\"%d\" height=\"128\" rx=\"12\" fill=\"%s\"/>", x, ancho, envoltorio));
        s.append("<circle cx=\"200\" cy=\"222\" r=\"34\" fill=\"#ffffff\" fill-opacity=\"0.9\"/>");
        s.append(f("<circle cx=\"200\" cy=\"222\" r=\"20\" fill=\"%s\"/>", choco));
        s.append(f("<rect x=\"%d\" y=\"170\" width=\"10\" height=\"108\" rx=\"5\" fill=\"#ffffff\" fill-opacity=\"0.2\"/></g>", x + 10));
    }

    private static void bombones(StringBuilder s) {
        String[] rellenos = {"#5c3d2e", "#8d5524", "#c9a27a", "#3a2318", "#b5654a", "#d6a5b4"};
        s.append("<rect x=\"80\" y=\"108\" width=\"240\" height=\"168\" rx=\"20\" fill=\"#3d2b1f\"/>");
        s.append("<rect x=\"92\" y=\"120\" width=\"216\" height=\"144\" rx=\"14\" fill=\"#5a4030\"/>");
        for (int i = 0; i < 6; i++) {
            int cx = 145 + (i % 3) * 55;
            int cy = 158 + (i / 3) * 62;
            s.append(f("<circle cx=\"%d\" cy=\"%d\" r=\"26\" fill=\"#f1e4d3\"/>", cx, cy));
            s.append(f("<circle cx=\"%d\" cy=\"%d\" r=\"20\" fill=\"%s\"/>", cx, cy, rellenos[i]));
            s.append(f("<ellipse cx=\"%d\" cy=\"%d\" rx=\"8\" ry=\"4\" fill=\"#ffffff\" fill-opacity=\"0.35\"/>", cx - 6, cy - 9));
        }
    }

    private static void huevo(StringBuilder s, String c) {
        s.append("<clipPath id=\"huevo\"><ellipse cx=\"200\" cy=\"172\" rx=\"82\" ry=\"104\"/></clipPath>");
        s.append(f("<ellipse cx=\"200\" cy=\"172\" rx=\"82\" ry=\"104\" fill=\"%s\"/>", c));
        s.append("<g clip-path=\"url(#huevo)\">");
        s.append("<rect x=\"100\" y=\"120\" width=\"200\" height=\"24\" fill=\"#fcc419\"/>");
        s.append("<rect x=\"100\" y=\"190\" width=\"200\" height=\"24\" fill=\"#fcc419\"/>");
        for (int i = 0; i < 7; i++) {
            s.append(f("<circle cx=\"%d\" cy=\"%d\" r=\"6\" fill=\"#ffffff\" fill-opacity=\"0.7\"/>", 135 + i * 22, i % 2 == 0 ? 160 : 168));
            s.append(f("<circle cx=\"%d\" cy=\"%d\" r=\"6\" fill=\"#ff8787\"/>", 146 + i * 22, i % 2 == 0 ? 245 : 238));
        }
        s.append("</g><ellipse cx=\"165\" cy=\"110\" rx=\"14\" ry=\"32\" fill=\"#ffffff\" fill-opacity=\"0.3\" ");
        s.append("transform=\"rotate(20 165 110)\"/>");
        s.append(f("<rect x=\"150\" y=\"272\" width=\"100\" height=\"14\" rx=\"7\" fill=\"%s\"/>", oscuro(c)));
    }

    private static void caramelos(StringBuilder s, boolean surtidos, String c) {
        String[] col = surtidos ? new String[]{MULTI[0], MULTI[3], MULTI[1]} : new String[]{c, oscuro(c), mezclar(c, "#ffffff", 0.2)};
        caramelo(s, 130, 235, 34, col[1]);
        caramelo(s, 275, 235, 34, col[2]);
        caramelo(s, 200, 150, 44, col[0]);
    }

    private static void caramelo(StringBuilder s, double cx, double cy, double r, String col) {
        s.append(f("<g transform=\"translate(%s %s)\">", num(cx), num(cy)));
        s.append(f("<polygon points=\"%s,0 %s,%s %s,%s\" fill=\"%s\"/>", num(-r * 0.9), num(-r * 1.9), num(-r * 0.6),
                num(-r * 1.9), num(r * 0.6), claro(col)));
        s.append(f("<polygon points=\"%s,0 %s,%s %s,%s\" fill=\"%s\"/>", num(r * 0.9), num(r * 1.9), num(-r * 0.6),
                num(r * 1.9), num(r * 0.6), claro(col)));
        s.append(f("<circle r=\"%s\" fill=\"%s\"/>", num(r), col));
        s.append(f("<path d=\"M%s %s Q0 %s %s %s\" fill=\"none\" stroke=\"#ffffff\" stroke-opacity=\"0.55\" stroke-width=\"%s\" ",
                num(-r * 0.7), num(-r * 0.4), num(r * 0.15), num(r * 0.7), num(-r * 0.4), num(r * 0.18)));
        s.append("stroke-linecap=\"round\"/>");
        s.append(f("<ellipse cx=\"%s\" cy=\"%s\" rx=\"%s\" ry=\"%s\" fill=\"#ffffff\" fill-opacity=\"0.4\"/></g>",
                num(-r * 0.35), num(-r * 0.5), num(r * 0.28), num(r * 0.14)));
    }

    private static void chupetines(StringBuilder s) {
        chupetin(s, 112, 140, 40, MULTI[3], -18);
        chupetin(s, 288, 140, 40, MULTI[4], 18);
        chupetin(s, 200, 115, 54, MULTI[0], 0);
    }

    private static void chupetin(StringBuilder s, double cx, double cy, double r, String col, double ang) {
        s.append(f("<g transform=\"translate(%s %s) rotate(%s)\">", num(cx), num(cy), num(ang)));
        s.append(f("<rect x=\"-4\" y=\"0\" width=\"8\" height=\"%s\" rx=\"4\" fill=\"#f1f3f5\" stroke=\"#ced4da\"/>", num(r * 3.1)));
        s.append(f("<circle r=\"%s\" fill=\"%s\"/>", num(r), col));
        s.append(f("<path d=\"M%s 0 a%s %s 0 1 1 %s 0 a%s %s 0 1 1 %s 0 a%s %s 0 1 1 %s 0 a%s %s 0 1 1 %s 0\" ",
                num(-r * 0.15), num(r * 0.15), num(r * 0.15), num(r * 0.3), num(r * 0.3), num(r * 0.3), num(-r * 0.6),
                num(r * 0.45), num(r * 0.45), num(r * 0.9), num(r * 0.6), num(r * 0.6), num(-r * 1.2)));
        s.append(f("fill=\"none\" stroke=\"#ffffff\" stroke-opacity=\"0.8\" stroke-width=\"%s\" stroke-linecap=\"round\"/>", num(r * 0.12)));
        s.append(f("<ellipse cx=\"%s\" cy=\"%s\" rx=\"%s\" ry=\"%s\" fill=\"#ffffff\" fill-opacity=\"0.35\" ",
                num(-r * 0.45), num(-r * 0.6), num(r * 0.22), num(r * 0.1)));
        s.append("transform=\"rotate(-30)\"/></g>");
    }

    private static void ositos(StringBuilder s, boolean acidos) {
        osito(s, 108, 215, 0.78, MULTI[3], acidos);
        osito(s, 292, 215, 0.78, MULTI[1], acidos);
        osito(s, 200, 175, 1.0, MULTI[0], acidos);
    }

    private static void osito(StringBuilder s, double cx, double cy, double k, String col, boolean azucar) {
        s.append(f("<g transform=\"translate(%s %s) scale(%s)\">", num(cx), num(cy), num(k)));
        s.append(f("<g fill=\"%s\">", col));
        s.append("<circle cx=\"-27\" cy=\"-52\" r=\"13\"/><circle cx=\"27\" cy=\"-52\" r=\"13\"/>");
        s.append("<ellipse cx=\"-42\" cy=\"6\" rx=\"15\" ry=\"26\" transform=\"rotate(20 -42 6)\"/>");
        s.append("<ellipse cx=\"42\" cy=\"6\" rx=\"15\" ry=\"26\" transform=\"rotate(-20 42 6)\"/>");
        s.append("<ellipse cx=\"-22\" cy=\"68\" rx=\"19\" ry=\"15\"/><ellipse cx=\"22\" cy=\"68\" rx=\"19\" ry=\"15\"/>");
        s.append("<ellipse cx=\"0\" cy=\"26\" rx=\"40\" ry=\"46\"/><circle cx=\"0\" cy=\"-30\" r=\"35\"/></g>");
        s.append("<ellipse cx=\"0\" cy=\"32\" rx=\"22\" ry=\"28\" fill=\"#ffffff\" fill-opacity=\"0.25\"/>");
        s.append("<circle cx=\"-12\" cy=\"-35\" r=\"3.5\" fill=\"#3b2a20\"/><circle cx=\"12\" cy=\"-35\" r=\"3.5\" fill=\"#3b2a20\"/>");
        s.append("<ellipse cx=\"0\" cy=\"-22\" rx=\"8\" ry=\"6\" fill=\"#ffffff\" fill-opacity=\"0.45\"/>");
        s.append("<ellipse cx=\"-14\" cy=\"-50\" rx=\"9\" ry=\"4\" fill=\"#ffffff\" fill-opacity=\"0.5\"/>");
        if (azucar) {
            s.append("<g fill=\"#ffffff\" fill-opacity=\"0.9\">");
            double[][] p = {{-28, 20}, {25, 4}, {8, 50}, {-12, 70}, {30, 40}, {-30, -20}, {26, -20}, {0, 12}};
            for (double[] q : p) {
                s.append(f("<circle cx=\"%s\" cy=\"%s\" r=\"2.4\"/>", num(q[0]), num(q[1])));
            }
            s.append("</g>");
        }
        s.append("</g>");
    }

    private static void aros(StringBuilder s) {
        aro(s, 125, 225, 44, MULTI[3]);
        aro(s, 275, 225, 44, MULTI[2]);
        aro(s, 200, 150, 58, MULTI[0]);
    }

    private static void aro(StringBuilder s, double cx, double cy, double r, String col) {
        s.append(f("<circle cx=\"%s\" cy=\"%s\" r=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"%s\"/>", num(cx), num(cy),
                num(r), col, num(r * 0.55)));
        s.append(f("<circle cx=\"%s\" cy=\"%s\" r=\"%s\" fill=\"none\" stroke=\"#ffffff\" stroke-opacity=\"0.25\" stroke-width=\"%s\"/>",
                num(cx), num(cy), num(r - r * 0.12), num(r * 0.12)));
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            s.append(f("<circle cx=\"%s\" cy=\"%s\" r=\"2.4\" fill=\"#ffffff\" fill-opacity=\"0.9\"/>",
                    num(cx + Math.cos(a) * r * (0.82 + (i % 2) * 0.3)), num(cy + Math.sin(a) * r * (0.82 + (i % 2) * 0.3))));
        }
    }

    private static void tuboPastillas(StringBuilder s, String c) {
        s.append(f("<rect x=\"150\" y=\"88\" width=\"100\" height=\"192\" rx=\"16\" fill=\"%s\"/>", c));
        s.append("<rect x=\"150\" y=\"150\" width=\"100\" height=\"76\" fill=\"#ffffff\" fill-opacity=\"0.88\"/>");
        for (int i = 0; i < 3; i++) {
            s.append(f("<circle cx=\"%d\" cy=\"188\" r=\"11\" fill=\"%s\"/>", 172 + i * 28, MULTI[(i * 2) % MULTI.length]));
        }
        s.append(f("<rect x=\"144\" y=\"76\" width=\"112\" height=\"24\" rx=\"8\" fill=\"%s\"/>", oscuro(c)));
        s.append("<rect x=\"160\" y=\"100\" width=\"10\" height=\"170\" rx=\"5\" fill=\"#ffffff\" fill-opacity=\"0.22\"/>");
        double[][] sueltas = {{98, 262, 18}, {128, 276, 15}, {300, 266, 18}, {335, 250, 14}};
        for (int i = 0; i < sueltas.length; i++) {
            s.append(f("<ellipse cx=\"%s\" cy=\"%s\" rx=\"%s\" ry=\"%s\" fill=\"%s\"/>", num(sueltas[i][0]), num(sueltas[i][1]),
                    num(sueltas[i][2]), num(sueltas[i][2] * 0.7), MULTI[(i * 2 + 1) % MULTI.length]));
        }
    }

    private static void malvaviscos(StringBuilder s) {
        marshmallow(s, 128, 215, 78, 66, "#ffd6e7", -18);
        marshmallow(s, 272, 215, 78, 66, "#fff3bf", 16);
        marshmallow(s, 200, 150, 92, 78, "#ffffff", 4);
        marshmallow(s, 200, 245, 84, 62, "#c5f6fa", -3);
    }

    private static void marshmallow(StringBuilder s, double cx, double cy, double w, double h, String col, double rot) {
        s.append(f("<g transform=\"translate(%s %s) rotate(%s)\">", num(cx), num(cy), num(rot)));
        s.append(f("<rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" rx=\"%s\" fill=\"%s\" stroke=\"#dee2e6\"/>",
                num(-w / 2), num(-h / 2), num(w), num(h), num(h * 0.4), col));
        s.append(f("<rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" rx=\"%s\" fill=\"#ffffff\" fill-opacity=\"0.65\"/></g>",
                num(-w * 0.34), num(-h * 0.34), num(w * 0.32), num(h * 0.12), num(h * 0.06)));
    }

    // ------------------------------------------------------------------ Chicles y galletitas

    private static void chicles(StringBuilder s, String c) {
        int[] giros = {-12, 0, 12};
        for (int i = 0; i < 3; i++) {
            s.append(f("<g transform=\"rotate(%d %d 175)\"><rect x=\"%d\" y=\"62\" width=\"38\" height=\"118\" rx=\"4\" ",
                    giros[i], 200 + (i - 1) * 40, 162 + i * 40));
            s.append("fill=\"#f1f3f5\" stroke=\"#ced4da\"/></g>");
        }
        s.append(f("<rect x=\"115\" y=\"150\" width=\"170\" height=\"130\" rx=\"10\" fill=\"%s\"/>", c));
        s.append("<rect x=\"115\" y=\"192\" width=\"170\" height=\"44\" fill=\"#ffffff\" fill-opacity=\"0.9\"/>");
        s.append(f("<ellipse cx=\"200\" cy=\"214\" rx=\"26\" ry=\"11\" fill=\"%s\" transform=\"rotate(-20 200 214)\"/>", c));
        s.append(f("<rect x=\"115\" y=\"150\" width=\"170\" height=\"10\" fill=\"%s\"/>", oscuro(c)));
        s.append(f("<rect x=\"115\" y=\"270\" width=\"170\" height=\"10\" rx=\"5\" fill=\"%s\"/>", oscuro(c)));
    }

    private static void globos(StringBuilder s, String c) {
        s.append(f("<circle cx=\"200\" cy=\"140\" r=\"82\" fill=\"%s\" fill-opacity=\"0.92\"/>", c));
        s.append(f("<circle cx=\"200\" cy=\"140\" r=\"82\" fill=\"none\" stroke=\"%s\" stroke-width=\"4\"/>", oscuro(c)));
        s.append("<ellipse cx=\"168\" cy=\"106\" rx=\"24\" ry=\"13\" fill=\"#ffffff\" fill-opacity=\"0.6\" transform=\"rotate(-35 168 106)\"/>");
        for (int i = 0; i < 3; i++) {
            s.append(f("<circle cx=\"%d\" cy=\"%d\" r=\"24\" fill=\"%s\"/>", 135 + i * 65, 262, MULTI[(i * 2) % MULTI.length]));
            s.append(f("<ellipse cx=\"%d\" cy=\"%d\" rx=\"8\" ry=\"4\" fill=\"#ffffff\" fill-opacity=\"0.5\"/>", 128 + i * 65, 252));
        }
    }

    private static void galletas(StringBuilder s, boolean chips) {
        galleta(s, 245, 135, 62, chips);
        galleta(s, 150, 190, 68, chips);
        galleta(s, 255, 225, 58, chips);
    }

    private static void galleta(StringBuilder s, double cx, double cy, double r, boolean chips) {
        double[][] p = {{-0.35, -0.3}, {0.3, -0.35}, {0.1, 0.05}, {-0.4, 0.25}, {0.45, 0.2}, {-0.05, -0.6}, {0.1, 0.55}};
        s.append(f("<circle cx=\"%s\" cy=\"%s\" r=\"%s\" fill=\"#e0a85c\" stroke=\"#b87c34\" stroke-width=\"3\"/>", num(cx), num(cy), num(r)));
        s.append(f("<circle cx=\"%s\" cy=\"%s\" r=\"%s\" fill=\"none\" stroke=\"#f2c789\" stroke-width=\"3\"/>", num(cx), num(cy), num(r - 7)));
        for (double[] q : p) {
            if (chips) {
                s.append(f("<ellipse cx=\"%s\" cy=\"%s\" rx=\"%s\" ry=\"%s\" fill=\"#4a2c1a\" transform=\"rotate(%s %s %s)\"/>",
                        num(cx + q[0] * r), num(cy + q[1] * r), num(r * 0.12), num(r * 0.08), num(q[0] * 120),
                        num(cx + q[0] * r), num(cy + q[1] * r)));
            } else {
                s.append(f("<ellipse cx=\"%s\" cy=\"%s\" rx=\"%s\" ry=\"%s\" fill=\"#ffffff\" fill-opacity=\"0.85\" transform=\"rotate(%s %s %s)\"/>",
                        num(cx + q[0] * r), num(cy + q[1] * r), num(r * 0.13), num(r * 0.05), num(q[1] * 150),
                        num(cx + q[0] * r), num(cy + q[1] * r)));
            }
        }
    }

    private static void oblea(StringBuilder s, String c) {
        s.append("<clipPath id=\"oblea\"><rect x=\"98\" y=\"96\" width=\"204\" height=\"134\" rx=\"14\"/></clipPath>");
        s.append("<g transform=\"rotate(-10 200 175)\"><g clip-path=\"url(#oblea)\">");
        for (int i = 0; i < 8; i++) {
            s.append(f("<rect x=\"98\" y=\"%d\" width=\"204\" height=\"17\" fill=\"%s\"/>", 96 + i * 17, i % 2 == 0 ? "#e8c07a" : c));
        }
        s.append("</g><rect x=\"98\" y=\"96\" width=\"204\" height=\"134\" rx=\"14\" fill=\"none\" stroke=\"#b87c34\" stroke-width=\"3\"/>");
        s.append("<rect x=\"108\" y=\"104\" width=\"60\" height=\"8\" rx=\"4\" fill=\"#ffffff\" fill-opacity=\"0.35\"/></g>");
        s.append(f("<rect x=\"140\" y=\"250\" width=\"120\" height=\"22\" rx=\"11\" fill=\"%s\"/>", oscuro(c)));
    }

    private static void barquillos(StringBuilder s) {
        barquillo(s, 200, 150, -18);
        barquillo(s, 200, 225, 12);
    }

    private static void barquillo(StringBuilder s, double cx, double cy, double rot) {
        s.append(f("<g transform=\"translate(%s %s) rotate(%s)\">", num(cx), num(cy), num(rot)));
        s.append("<rect x=\"-110\" y=\"-22\" width=\"220\" height=\"44\" rx=\"22\" fill=\"#e8c07a\" stroke=\"#b87c34\" stroke-width=\"2\"/>");
        s.append("<g stroke=\"#c58f3e\" stroke-width=\"4\" stroke-linecap=\"round\">");
        for (int x = -88; x <= 40; x += 20) {
            s.append(f("<line x1=\"%d\" y1=\"-16\" x2=\"%d\" y2=\"16\"/>", x, x + 12));
        }
        s.append("</g><rect x=\"52\" y=\"-22\" width=\"58\" height=\"44\" rx=\"22\" fill=\"#5c3d2e\"/>");
        s.append("<rect x=\"52\" y=\"-22\" width=\"20\" height=\"44\" fill=\"#5c3d2e\"/></g>");
    }

    private static void crackers(StringBuilder s, String c) {
        cracker(s, 140, 205, -12, c);
        cracker(s, 262, 195, 10, c);
        cracker(s, 200, 130, 4, c);
    }

    private static void cracker(StringBuilder s, double cx, double cy, double rot, String c) {
        s.append(f("<g transform=\"translate(%s %s) rotate(%s)\">", num(cx), num(cy), num(rot)));
        s.append(f("<rect x=\"-62\" y=\"-62\" width=\"124\" height=\"124\" rx=\"10\" fill=\"%s\" stroke=\"%s\" stroke-width=\"3\"/>", c, oscuro(c)));
        s.append(f("<rect x=\"-52\" y=\"-52\" width=\"104\" height=\"104\" rx=\"6\" fill=\"none\" stroke=\"%s\" stroke-width=\"2\" stroke-dasharray=\"4 5\"/>", claro(c)));
        s.append(f("<g fill=\"%s\">", oscuro(c)));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                s.append(f("<circle cx=\"%d\" cy=\"%d\" r=\"4\"/>", -26 + i * 26, -26 + j * 26));
            }
        }
        s.append("</g></g>");
    }

    private static void grisines(StringBuilder s) {
        int[] giros = {-18, -12, -6, 0, 6, 12, 18};
        for (int i = 0; i < giros.length; i++) {
            s.append(f("<g transform=\"rotate(%d 200 285)\"><rect x=\"192\" y=\"70\" width=\"16\" height=\"210\" rx=\"8\" fill=\"%s\" stroke=\"%s\"/>",
                    giros[i], i % 2 == 0 ? "#e2b36a" : "#d49e4f", "#b87c34"));
            s.append("<rect x=\"196\" y=\"84\" width=\"3\" height=\"170\" rx=\"1.5\" fill=\"#ffffff\" fill-opacity=\"0.3\"/></g>");
        }
    }

    // ------------------------------------------------------------------ Snacks

    private static void bolsa(StringBuilder s, String c, String tipo) {
        StringBuilder pts = new StringBuilder();
        for (int i = 0; i <= 12; i++) {
            pts.append(f("%s,%d ", num(120 + i * 13.33), i % 2 == 0 ? 68 : 82));
        }
        pts.append("288,282 112,282");
        s.append(f("<polygon points=\"%s\" fill=\"%s\"/>", pts, c));
        s.append(f("<rect x=\"120\" y=\"82\" width=\"160\" height=\"22\" fill=\"%s\" fill-opacity=\"0.35\"/>", oscuro(c)));
        s.append(f("<rect x=\"114\" y=\"262\" width=\"172\" height=\"20\" fill=\"%s\" fill-opacity=\"0.3\"/>", oscuro(c)));
        s.append("<rect x=\"146\" y=\"135\" width=\"108\" height=\"96\" rx=\"16\" fill=\"#ffffff\" fill-opacity=\"0.92\"/>");
        s.append(f("<circle cx=\"200\" cy=\"183\" r=\"26\" fill=\"%s\"/>", c));
        s.append(f("<circle cx=\"192\" cy=\"175\" r=\"8\" fill=\"%s\"/>", claro(c)));
        s.append("<rect x=\"130\" y=\"110\" width=\"10\" height=\"150\" rx=\"5\" fill=\"#ffffff\" fill-opacity=\"0.22\"/>");
        switch (tipo) {
            case "papas" -> {
                for (double[] p : new double[][]{{78, 262, -20}, {325, 268, 25}, {62, 228, 15}}) {
                    s.append(f("<ellipse cx=\"%s\" cy=\"%s\" rx=\"26\" ry=\"18\" fill=\"#f7c948\" stroke=\"#d4a017\" stroke-width=\"2\" transform=\"rotate(%s %s %s)\"/>",
                            num(p[0]), num(p[1]), num(p[2]), num(p[0]), num(p[1])));
                }
            }
            case "mani", "garrapinada" -> {
                String col = "mani".equals(tipo) ? MANI : "#b5651d";
                for (double[] p : new double[][]{{78, 266, -25}, {325, 270, 20}, {64, 232, 30}, {340, 238, -10}}) {
                    mani(s, p[0], p[1], p[2], col);
                }
            }
            case "palitos" -> {
                for (int i = 0; i < 4; i++) {
                    s.append(f("<rect x=\"%d\" y=\"%d\" width=\"9\" height=\"54\" rx=\"4\" fill=\"#f2a33a\" stroke=\"#c97d12\" transform=\"rotate(%d %d %d)\"/>",
                            56 + i * 12, 224, 62 + i * 14 - 40, 60 + i * 12, 250));
                }
            }
            case "maiz" -> {
                for (double[] p : new double[][]{{78, 262, 0}, {325, 268, 0}, {58, 226, 0}}) {
                    s.append(f("<polygon points=\"%s,%s %s,%s %s,%s\" fill=\"#f5b82e\" stroke=\"#d99a12\" stroke-width=\"2\" stroke-linejoin=\"round\"/>",
                            num(p[0] - 22), num(p[1] + 16), num(p[0]), num(p[1] - 20), num(p[0] + 22), num(p[1] + 16)));
                }
            }
            default -> {
                for (double[] p : new double[][]{{80, 262}, {104, 270}, {62, 240}, {322, 268}, {346, 258}, {328, 240}}) {
                    s.append(f("<circle cx=\"%s\" cy=\"%s\" r=\"15\" fill=\"#fff3d6\" stroke=\"#f1d9a0\" stroke-width=\"2\"/>", num(p[0]), num(p[1])));
                    s.append(f("<circle cx=\"%s\" cy=\"%s\" r=\"6\" fill=\"#ff8787\" fill-opacity=\"0.8\"/>", num(p[0] + 3), num(p[1] + 2)));
                }
            }
        }
    }

    private static void mani(StringBuilder s, double cx, double cy, double rot, String col) {
        s.append(f("<g transform=\"translate(%s %s) rotate(%s)\"><circle cx=\"-9\" cy=\"0\" r=\"13\" fill=\"%s\" stroke=\"%s\" stroke-width=\"2\"/>",
                num(cx), num(cy), num(rot), col, oscuro(col)));
        s.append(f("<circle cx=\"9\" cy=\"0\" r=\"13\" fill=\"%s\" stroke=\"%s\" stroke-width=\"2\"/>", col, oscuro(col)));
        s.append(f("<rect x=\"-9\" y=\"-9\" width=\"18\" height=\"18\" fill=\"%s\"/></g>", col));
    }

    // ------------------------------------------------------------------ Bebidas

    private static void gaseosas(StringBuilder s, String n, String c) {
        String franja = n.contains("cola") ? "#e03131" : oscuro(c);
        if (n.contains("2,25")) {
            botella(s, 150, 188, 0.9, c, franja, "#e03131");
            botella(s, 275, 210, 0.72, c, franja, "#e03131");
        } else {
            botellas(s, c, "#ffffff", franja, false);
        }
    }

    /** Pack de tres botellas (la del centro, al frente). */
    private static void botellas(StringBuilder s, String liquido, String franja, String tapa, boolean grande) {
        double k = grande ? 0.82 : 0.76;
        botella(s, 128, 205, k * 0.88, liquido, franja, tapa);
        botella(s, 272, 205, k * 0.88, liquido, franja, tapa);
        botella(s, 200, 188, k, liquido, franja, tapa);
    }

    private static void botella(StringBuilder s, double tx, double ty, double k, String liquido, String franja, String tapa) {
        String forma = "M-22 -94 v-0 h44 v0 c0 18 38 30 38 62 v150 c0 14 -10 24 -24 24 h-72 c-14 0 -24 -10 -24 -24 v-150 "
                + "c0 -32 38 -44 38 -62 z";
        s.append(f("<g transform=\"translate(%s %s) scale(%s)\">", num(tx), num(ty), num(k)));
        s.append(f("<rect x=\"-24\" y=\"-132\" width=\"48\" height=\"20\" rx=\"5\" fill=\"%s\"/>", tapa));
        s.append(f("<rect x=\"-19\" y=\"-112\" width=\"38\" height=\"22\" fill=\"%s\" fill-opacity=\"0.85\"/>", liquido));
        s.append(f("<path d=\"%s\" fill=\"%s\"/>", forma, liquido));
        s.append(f("<rect x=\"-60\" y=\"8\" width=\"120\" height=\"66\" fill=\"#ffffff\" fill-opacity=\"0.93\"/>"));
        s.append(f("<rect x=\"-60\" y=\"20\" width=\"120\" height=\"14\" fill=\"%s\"/>", franja));
        s.append(f("<circle cx=\"0\" cy=\"54\" r=\"10\" fill=\"%s\"/>", liquido));
        s.append("<rect x=\"-48\" y=\"-24\" width=\"10\" height=\"130\" rx=\"5\" fill=\"#ffffff\" fill-opacity=\"0.32\"/></g>");
    }

    private static void latas(StringBuilder s, String c) {
        lata(s, 120, 205, 0.8, c);
        lata(s, 280, 205, 0.8, c);
        lata(s, 200, 188, 0.95, c);
    }

    private static void lata(StringBuilder s, double tx, double ty, double k, String c) {
        s.append(f("<g transform=\"translate(%s %s) scale(%s)\">", num(tx), num(ty), num(k)));
        s.append(f("<rect x=\"-48\" y=\"-90\" width=\"96\" height=\"180\" rx=\"16\" fill=\"%s\"/>", c));
        s.append("<rect x=\"-48\" y=\"-34\" width=\"96\" height=\"76\" fill=\"#212529\"/>");
        s.append("<polygon points=\"10,-30 -18,12 -3,12 -10,38 20,-6 5,-6\" fill=\"#ffd43b\"/>");
        s.append("<ellipse cx=\"0\" cy=\"-90\" rx=\"46\" ry=\"10\" fill=\"#ced4da\"/>");
        s.append("<ellipse cx=\"0\" cy=\"-92\" rx=\"15\" ry=\"5\" fill=\"#adb5bd\" stroke=\"#868e96\"/>");
        s.append("<rect x=\"-38\" y=\"-76\" width=\"9\" height=\"150\" rx=\"4\" fill=\"#ffffff\" fill-opacity=\"0.3\"/></g>");
    }

    private static void sobres(StringBuilder s, String c) {
        sobre(s, 125, 195, -14, mezclar(c, "#ffffff", 0.15));
        sobre(s, 275, 195, 14, oscuro(c));
        sobre(s, 200, 185, 0, c);
    }

    private static void sobre(StringBuilder s, double cx, double cy, double rot, String c) {
        s.append(f("<g transform=\"translate(%s %s) rotate(%s)\">", num(cx), num(cy), num(rot)));
        s.append(f("<rect x=\"-55\" y=\"-85\" width=\"110\" height=\"170\" rx=\"5\" fill=\"%s\"/>", c));
        s.append(f("<rect x=\"-55\" y=\"-85\" width=\"110\" height=\"14\" fill=\"%s\" fill-opacity=\"0.4\"/>", oscuro(c)));
        s.append(f("<rect x=\"-55\" y=\"71\" width=\"110\" height=\"14\" fill=\"%s\" fill-opacity=\"0.4\"/>", oscuro(c)));
        s.append("<circle cx=\"0\" cy=\"0\" r=\"34\" fill=\"#ffffff\" fill-opacity=\"0.92\"/>");
        s.append(f("<circle cx=\"0\" cy=\"0\" r=\"27\" fill=\"%s\"/>", claro(c)));
        s.append("<g stroke=\"#ffffff\" stroke-width=\"2\">");
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 4;
            s.append(f("<line x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\"/>", num(Math.cos(a) * 27), num(Math.sin(a) * 27),
                    num(-Math.cos(a) * 27), num(-Math.sin(a) * 27)));
        }
        s.append("</g><rect x=\"-45\" y=\"-60\" width=\"8\" height=\"110\" rx=\"4\" fill=\"#ffffff\" fill-opacity=\"0.22\"/></g>");
    }

    private static void briks(StringBuilder s, String c) {
        brik(s, 130, 215, 0.7, mezclar(c, "#ffffff", 0.2));
        brik(s, 270, 215, 0.7, oscuro(c));
        brik(s, 200, 195, 0.95, c);
    }

    private static void brik(StringBuilder s, double tx, double ty, double k, String c) {
        s.append(f("<g transform=\"translate(%s %s) scale(%s)\">", num(tx), num(ty), num(k)));
        s.append(f("<polygon points=\"60,-70 90,-85 90,75 60,95\" fill=\"%s\"/>", oscuro(c)));
        s.append(f("<polygon points=\"-60,-70 -30,-85 90,-85 60,-70\" fill=\"%s\"/>", claro(c)));
        s.append(f("<rect x=\"-60\" y=\"-70\" width=\"120\" height=\"165\" fill=\"%s\"/>", c));
        s.append("<path d=\"M25 -83 L42 -135 L78 -142\" fill=\"none\" stroke=\"#ff6b6b\" stroke-width=\"7\" stroke-linecap=\"round\"/>");
        s.append("<circle cx=\"0\" cy=\"10\" r=\"38\" fill=\"#ffffff\" fill-opacity=\"0.93\"/>");
        s.append(f("<circle cx=\"0\" cy=\"10\" r=\"28\" fill=\"%s\"/>", claro(c)));
        s.append("<ellipse cx=\"-9\" cy=\"0\" rx=\"9\" ry=\"5\" fill=\"#ffffff\" fill-opacity=\"0.6\"/>");
        s.append("<rect x=\"-52\" y=\"-60\" width=\"8\" height=\"145\" rx=\"4\" fill=\"#ffffff\" fill-opacity=\"0.22\"/></g>");
    }

    // ------------------------------------------------------------------ Kiosco y almacén

    private static void barras(StringBuilder s, String n, String c) {
        String deco = n.contains("maní") || n.contains("turrón") ? "mani" : n.contains("frutas") ? "frutas" : "ninguno";
        barra(s, 200, 130, -6, c, deco);
        barra(s, 200, 195, 4, mezclar(c, "#ffffff", 0.25), deco);
        barra(s, 200, 258, -3, oscuro(c), deco);
    }

    private static void barra(StringBuilder s, double cx, double cy, double rot, String c, String deco) {
        s.append(f("<g transform=\"translate(%s %s) rotate(%s)\">", num(cx), num(cy), num(rot)));
        s.append(f("<polygon points=\"-100,-32 -118,-22 -108,-11 -118,0 -108,11 -118,22 -100,32\" fill=\"%s\"/>", oscuro(c)));
        s.append(f("<polygon points=\"100,-32 118,-22 108,-11 118,0 108,11 118,22 100,32\" fill=\"%s\"/>", oscuro(c)));
        s.append(f("<rect x=\"-100\" y=\"-32\" width=\"200\" height=\"64\" rx=\"8\" fill=\"%s\"/>", c));
        s.append("<rect x=\"-100\" y=\"-9\" width=\"200\" height=\"18\" fill=\"#ffffff\" fill-opacity=\"0.88\"/>");
        if ("mani".equals(deco)) {
            for (int i = -1; i <= 1; i++) {
                mani(s, i * 36, 0, i * 20, MANI);
            }
        } else if ("frutas".equals(deco)) {
            for (int i = -1; i <= 1; i++) {
                s.append(f("<circle cx=\"%d\" cy=\"0\" r=\"6\" fill=\"%s\"/>", i * 30, MULTI[(i + 1) * 2 % MULTI.length]));
            }
        }
        s.append("<rect x=\"-90\" y=\"-26\" width=\"60\" height=\"6\" rx=\"3\" fill=\"#ffffff\" fill-opacity=\"0.3\"/></g>");
    }

    private static void potes(StringBuilder s, String c, boolean grande) {
        pote(s, 118, 222, 0.7, mezclar(c, "#ffffff", 0.18), false);
        pote(s, 282, 222, 0.7, oscuro(c), false);
        pote(s, 200, 190, grande ? 1.05 : 0.95, c, true);
    }

    private static void pote(StringBuilder s, double tx, double ty, double k, String c, boolean cartel) {
        s.append(f("<g transform=\"translate(%s %s) scale(%s)\">", num(tx), num(ty), num(k)));
        s.append(f("<rect x=\"-70\" y=\"-70\" width=\"140\" height=\"172\" rx=\"24\" fill=\"%s\"/>", c));
        s.append(f("<rect x=\"-76\" y=\"-104\" width=\"152\" height=\"38\" rx=\"9\" fill=\"%s\"/>", oscuro(c)));
        s.append("<rect x=\"-56\" y=\"-28\" width=\"112\" height=\"84\" rx=\"12\" fill=\"#ffffff\" fill-opacity=\"0.93\"/>");
        s.append(f("<rect x=\"-56\" y=\"-12\" width=\"112\" height=\"12\" fill=\"%s\"/>", c));
        s.append(f("<circle cx=\"0\" cy=\"26\" r=\"%d\" fill=\"%s\"/>", cartel ? 18 : 14, claro(c)));
        s.append("<rect x=\"-62\" y=\"-60\" width=\"10\" height=\"150\" rx=\"5\" fill=\"#ffffff\" fill-opacity=\"0.22\"/></g>");
    }

    private static void panDulce(StringBuilder s, String c) {
        s.append("<g transform=\"rotate(-8 200 190)\">");
        s.append(f("<rect x=\"92\" y=\"118\" width=\"216\" height=\"124\" rx=\"18\" fill=\"%s\"/>", c));
        s.append(f("<rect x=\"92\" y=\"118\" width=\"216\" height=\"28\" rx=\"14\" fill=\"%s\"/>", claro(c)));
        s.append("<rect x=\"128\" y=\"164\" width=\"144\" height=\"58\" rx=\"9\" fill=\"#ffffff\" fill-opacity=\"0.93\"/>");
        s.append(f("<circle cx=\"200\" cy=\"193\" r=\"16\" fill=\"%s\"/>", oscuro(c)));
        s.append(f("<rect x=\"246\" y=\"250\" width=\"70\" height=\"28\" rx=\"7\" fill=\"%s\"/>", c));
        s.append(f("<rect x=\"250\" y=\"254\" width=\"22\" height=\"6\" rx=\"3\" fill=\"%s\"/></g>", claro(c)));
    }

    private static void latasCacao(StringBuilder s, String c) {
        lataCacao(s, 118, 222, 0.7, oscuro(c));
        lataCacao(s, 282, 222, 0.7, mezclar(c, "#ffffff", 0.15));
        lataCacao(s, 200, 190, 0.95, c);
    }

    private static void lataCacao(StringBuilder s, double tx, double ty, double k, String c) {
        s.append(f("<g transform=\"translate(%s %s) scale(%s)\">", num(tx), num(ty), num(k)));
        s.append(f("<rect x=\"-60\" y=\"-100\" width=\"120\" height=\"200\" rx=\"14\" fill=\"%s\"/>", c));
        s.append(f("<ellipse cx=\"0\" cy=\"-100\" rx=\"60\" ry=\"12\" fill=\"%s\"/>", claro(c)));
        s.append("<rect x=\"-60\" y=\"-44\" width=\"120\" height=\"96\" fill=\"#ffe8cc\"/>");
        s.append(f("<rect x=\"-26\" y=\"-14\" width=\"40\" height=\"38\" rx=\"8\" fill=\"%s\"/>", c));
        s.append(f("<path d=\"M14 -6 h8 a10 10 0 0 1 0 22 h-8\" fill=\"none\" stroke=\"%s\" stroke-width=\"6\"/>", c));
        s.append(f("<path d=\"M-14 -22 q-6 -10 0 -18 M2 -22 q-6 -10 0 -18\" fill=\"none\" stroke=\"%s\" stroke-width=\"3\" stroke-linecap=\"round\"/>", c));
        s.append("<rect x=\"-50\" y=\"-90\" width=\"9\" height=\"180\" rx=\"4\" fill=\"#ffffff\" fill-opacity=\"0.22\"/></g>");
    }

    // ------------------------------------------------------------------ Repostería

    private static void cajasPremezcla(StringBuilder s, String c, boolean brownie) {
        cajaPremezcla(s, 120, 215, 0.68, mezclar(c, "#ffffff", 0.2), brownie);
        cajaPremezcla(s, 282, 215, 0.68, oscuro(c), brownie);
        cajaPremezcla(s, 200, 195, 0.95, c, brownie);
    }

    private static void cajaPremezcla(StringBuilder s, double tx, double ty, double k, String c, boolean brownie) {
        String torta = brownie ? "#4a2c1a" : "#f1d9a0";
        s.append(f("<g transform=\"translate(%s %s) scale(%s)\">", num(tx), num(ty), num(k)));
        s.append(f("<polygon points=\"65,-95 100,-110 100,75 65,90\" fill=\"%s\"/>", oscuro(c)));
        s.append(f("<polygon points=\"-65,-95 -30,-110 100,-110 65,-95\" fill=\"%s\"/>", claro(c)));
        s.append(f("<rect x=\"-65\" y=\"-95\" width=\"130\" height=\"185\" fill=\"%s\"/>", c));
        s.append(f("<rect x=\"-65\" y=\"40\" width=\"130\" height=\"50\" fill=\"%s\" fill-opacity=\"0.28\"/>", oscuro(c)));
        s.append("<circle cx=\"0\" cy=\"-18\" r=\"46\" fill=\"#ffffff\" fill-opacity=\"0.94\"/>");
        s.append(f("<rect x=\"-32\" y=\"-12\" width=\"64\" height=\"28\" rx=\"7\" fill=\"%s\"/>", torta));
        s.append(f("<ellipse cx=\"0\" cy=\"-14\" rx=\"32\" ry=\"9\" fill=\"%s\"/>", brownie ? "#6b3f24" : "#ffffff"));
        s.append("<circle cx=\"0\" cy=\"-22\" r=\"7\" fill=\"#e03131\"/>");
        s.append("<rect x=\"-56\" y=\"-85\" width=\"9\" height=\"165\" rx=\"4\" fill=\"#ffffff\" fill-opacity=\"0.22\"/></g>");
    }

    private static void frascoGrajeas(StringBuilder s, String c) {
        s.append("<g transform=\"translate(200 185)\">");
        s.append("<rect x=\"-68\" y=\"-62\" width=\"136\" height=\"164\" rx=\"28\" fill=\"#f8f9fa\" stroke=\"#ced4da\" stroke-width=\"3\"/>");
        s.append("<rect x=\"-62\" y=\"-6\" width=\"124\" height=\"102\" rx=\"24\" fill=\"#ffffff\" fill-opacity=\"0.6\"/>");
        int semilla = 7;
        for (int i = 0; i < 46; i++) {
            semilla = (semilla * 1103515245 + 12345) & 0x7fffffff;
            int x = -50 + (semilla >> 4) % 100;
            semilla = (semilla * 1103515245 + 12345) & 0x7fffffff;
            int y = -2 + (semilla >> 4) % 92;
            s.append(f("<circle cx=\"%d\" cy=\"%d\" r=\"%d\" fill=\"%s\"/>", x, y, 5 + i % 3, MULTI[i % MULTI.length]));
        }
        s.append(f("<rect x=\"-74\" y=\"-98\" width=\"148\" height=\"38\" rx=\"9\" fill=\"%s\"/>", c));
        s.append("<rect x=\"-58\" y=\"-52\" width=\"9\" height=\"140\" rx=\"4\" fill=\"#ffffff\" fill-opacity=\"0.35\"/></g>");
    }

    // ------------------------------------------------------------------ Texto y utilidades

    private static void etiqueta(StringBuilder s, String nombre) {
        String linea1 = nombre;
        String linea2 = "";
        if (nombre.length() > 24) {
            int corte = nombre.lastIndexOf(' ', 24);
            corte = corte < 8 ? 24 : corte;
            linea1 = nombre.substring(0, corte).trim();
            linea2 = nombre.substring(corte).trim();
            if (linea2.length() > 28) {
                linea2 = linea2.substring(0, 27) + "…";
            }
        }
        s.append(f("<text x=\"200\" y=\"%d\" font-family=\"sans-serif\" font-size=\"22\" font-weight=\"bold\" text-anchor=\"middle\" fill=\"#343a40\">%s</text>",
                linea2.isEmpty() ? 345 : 336, xml(linea1)));
        if (!linea2.isEmpty()) {
            s.append(f("<text x=\"200\" y=\"364\" font-family=\"sans-serif\" font-size=\"22\" font-weight=\"bold\" text-anchor=\"middle\" fill=\"#343a40\">%s</text>",
                    xml(linea2)));
        }
    }

    private static String f(String formato, Object... args) {
        return String.format(Locale.ROOT, formato, args);
    }

    /** Número con un decimal como máximo, sin coma decimal (el SVG exige punto). */
    private static String num(double valor) {
        String t = String.format(Locale.ROOT, "%.1f", valor);
        return t.endsWith(".0") ? t.substring(0, t.length() - 2) : t;
    }

    private static String xml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
