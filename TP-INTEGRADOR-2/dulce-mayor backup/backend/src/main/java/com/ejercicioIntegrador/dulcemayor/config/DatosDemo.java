package com.ejercicioIntegrador.dulcemayor.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tablas de los datos de demostración que carga InicializadorDatosDemo. Son datos ficticios (comercios, personas,
 * productos y precios inventados) pensados solo para poder probar el sistema y los reportes; no deben usarse en producción.
 * Las coordenadas son aproximadas (cabecera de cada departamento).
 */
final class DatosDemo {

    private DatosDemo() {
    }

    static final String DOMINIO_CORREO = "@demo.dulcemayor.com.ar";
    static final String CLAVE_CLIENTES = "Cliente2026";

    // ------------------------------------------------------------------ Catálogo

    /** Subcategoría -> categoría. */
    static final Map<String, String> SUBCATEGORIAS = new LinkedHashMap<>();

    static {
        SUBCATEGORIAS.put("Alfajores", "Chocolates y alfajores");
        SUBCATEGORIAS.put("Chocolates", "Chocolates y alfajores");
        SUBCATEGORIAS.put("Caramelos", "Golosinas");
        SUBCATEGORIAS.put("Gomitas", "Golosinas");
        SUBCATEGORIAS.put("Chicles", "Golosinas");
        SUBCATEGORIAS.put("Galletitas dulces", "Galletitas y snacks");
        SUBCATEGORIAS.put("Galletitas saladas", "Galletitas y snacks");
        SUBCATEGORIAS.put("Snacks", "Galletitas y snacks");
        SUBCATEGORIAS.put("Gaseosas", "Bebidas");
        SUBCATEGORIAS.put("Jugos y aguas", "Bebidas");
        SUBCATEGORIAS.put("Energizantes e isotónicas", "Bebidas");
        SUBCATEGORIAS.put("Barras y turrones", "Kiosco y almacén");
        SUBCATEGORIAS.put("Dulces y untables", "Kiosco y almacén");
        SUBCATEGORIAS.put("Repostería", "Repostería");
    }

    /** codigo|nombre|descripcion|subcategoria|presentacion|unidadesPorPresentacion|enOferta(S/N)|precio. */
    static final List<String> PRODUCTOS = List.of(
            "ALF-001|Alfajor triple de chocolate|Display de alfajores triples con baño de chocolate|Alfajores|DISPLAY|24|N|18500",
            "ALF-002|Alfajor de dulce de leche|Display de alfajores rellenos de dulce de leche|Alfajores|DISPLAY|24|N|16800",
            "ALF-003|Alfajor de maicena|Caja de alfajores de maicena con coco|Alfajores|CAJA|12|N|9800",
            "ALF-004|Alfajor glaseado blanco|Display de alfajores con glaseado blanco|Alfajores|DISPLAY|18|N|15200",
            "ALF-005|Alfajor mini surtido|Bulto de alfajores mini en sabores surtidos|Alfajores|BULTO|60|N|21000",
            "CHO-001|Tableta de chocolate con leche 100 g|Pack de tabletas de chocolate con leche|Chocolates|PACK|12|N|17400",
            "CHO-002|Tableta de chocolate semiamargo 100 g|Pack de tabletas de chocolate semiamargo|Chocolates|PACK|12|N|18900",
            "CHO-003|Bombones surtidos 250 g|Caja de bombones rellenos surtidos|Chocolates|CAJA|10|N|32000",
            "CHO-004|Huevo de chocolate chico|Display de huevos de chocolate con sorpresa|Chocolates|DISPLAY|12|S|24800",
            "CHO-005|Chocolate relleno con maní|Display de chocolates individuales rellenos con maní|Chocolates|DISPLAY|20|N|13900",
            "CAR-001|Caramelos masticables frutales|Bulto de bolsas de caramelos masticables|Caramelos|BULTO|10|N|12600",
            "CAR-002|Caramelos duros de miel|Pack de bolsas de caramelos duros de miel|Caramelos|PACK|12|N|7400",
            "CAR-003|Caramelos de menta|Pack de bolsas de caramelos de menta|Caramelos|PACK|24|N|8900",
            "CAR-004|Caramelos rellenos|Bulto de bolsas de caramelos con relleno líquido|Caramelos|BULTO|12|S|11300",
            "CAR-005|Chupetines frutales|Display de chupetines de frutas|Caramelos|DISPLAY|50|N|9900",
            "GOM-001|Gomitas ositos|Display de bolsitas de gomitas con forma de osito|Gomitas|DISPLAY|24|N|14200",
            "GOM-002|Gomitas ácidas|Display de bolsitas de gomitas ácidas|Gomitas|DISPLAY|24|N|15100",
            "GOM-003|Gomitas aros de azúcar|Pack de bolsas de aros de goma con azúcar|Gomitas|PACK|12|N|8200",
            "GOM-004|Pastillas de goma|Display de tubos de pastillas de goma|Gomitas|DISPLAY|30|S|11800",
            "GOM-005|Marshmallows|Pack de bolsas de malvaviscos de colores|Gomitas|PACK|12|N|9600",
            "CHI-001|Chicles de menta|Display de chicles de menta|Chicles|DISPLAY|20|N|6500",
            "CHI-002|Chicles de frutilla|Display de chicles de frutilla|Chicles|DISPLAY|20|N|6500",
            "CHI-003|Globos de goma|Display de chicles globo|Chicles|DISPLAY|60|N|7900",
            "CHI-004|Chicle sin azúcar|Display de chicles sin azúcar|Chicles|DISPLAY|24|N|9200",
            "GAL-001|Galletitas con chips de chocolate|Pack de paquetes de galletitas con chips|Galletitas dulces|PACK|12|N|10800",
            "GAL-002|Galletitas rellenas de vainilla|Pack de paquetes de galletitas rellenas|Galletitas dulces|PACK|18|N|12500",
            "GAL-003|Galletitas de coco|Pack de paquetes de galletitas de coco|Galletitas dulces|PACK|12|N|8700",
            "GAL-004|Obleas rellenas|Display de obleas rellenas|Galletitas dulces|DISPLAY|24|S|13400",
            "GAL-005|Barquillos|Pack de paquetes de barquillos|Galletitas dulces|PACK|10|N|7600",
            "GAS-001|Crackers clásicos|Pack de paquetes de galletitas crackers|Galletitas saladas|PACK|20|N|9400",
            "GAS-002|Galletitas de queso|Pack de paquetes de galletitas saladas de queso|Galletitas saladas|PACK|20|N|10100",
            "GAS-003|Grisines|Pack de paquetes de grisines|Galletitas saladas|PACK|24|N|7100",
            "SNA-001|Papas fritas clásicas 65 g|Display de bolsas de papas fritas|Snacks|DISPLAY|24|N|19800",
            "SNA-002|Maní salado 150 g|Pack de bolsas de maní salado|Snacks|PACK|20|N|13600",
            "SNA-003|Palitos salados|Pack de bolsas de palitos salados|Snacks|PACK|24|N|11500",
            "SNA-004|Snack de maíz|Display de bolsas de snack de maíz|Snacks|DISPLAY|24|S|17200",
            "SNA-005|Pochoclo dulce|Pack de bolsas de pochoclo dulce|Snacks|PACK|20|N|12900",
            "BEB-001|Gaseosa cola 500 ml|Pack de gaseosas de cola|Gaseosas|PACK|12|N|14200",
            "BEB-002|Gaseosa lima limón 500 ml|Pack de gaseosas de lima limón|Gaseosas|PACK|12|N|13800",
            "BEB-003|Gaseosa naranja 500 ml|Pack de gaseosas de naranja|Gaseosas|PACK|12|N|13800",
            "BEB-004|Gaseosa cola 2,25 L|Pack de gaseosas de cola familiar|Gaseosas|PACK|6|N|15900",
            "JUG-001|Jugo en polvo de naranja|Caja de sobres de jugo en polvo de naranja|Jugos y aguas|CAJA|24|N|9800",
            "JUG-002|Jugo en polvo de limonada|Caja de sobres de jugo en polvo de limonada|Jugos y aguas|CAJA|24|N|9800",
            "JUG-003|Agua mineral 500 ml|Pack de botellas de agua mineral|Jugos y aguas|PACK|12|N|6900",
            "JUG-004|Agua saborizada pomelo 1,5 L|Pack de botellas de agua saborizada|Jugos y aguas|PACK|6|N|8800",
            "JUG-005|Jugo listo para beber 200 ml|Caja de jugos en envase individual|Jugos y aguas|CAJA|27|S|15400",
            "ENE-001|Bebida energizante 250 ml|Pack de latas de bebida energizante|Energizantes e isotónicas|PACK|24|N|28800",
            "ENE-002|Bebida isotónica 500 ml|Pack de botellas de bebida isotónica|Energizantes e isotónicas|PACK|12|N|16700",
            "TUR-001|Turrón de maní|Display de turrones de maní individuales|Barras y turrones|DISPLAY|40|N|12100",
            "TUR-002|Garrapiñada de maní|Display de bolsitas de garrapiñada|Barras y turrones|DISPLAY|30|N|11200",
            "TUR-003|Barra de cereal con frutas|Display de barras de cereal con frutas|Barras y turrones|DISPLAY|24|S|14800",
            "TUR-004|Barra de maní y miel|Display de barras de maní y miel|Barras y turrones|DISPLAY|24|N|13300",
            "TUR-005|Pasta de maní en barra|Display de barras de pasta de maní|Barras y turrones|DISPLAY|12|N|9600",
            "DUL-001|Dulce de leche 400 g|Pack de potes de dulce de leche clásico|Dulces y untables|PACK|12|N|21600",
            "DUL-002|Dulce de leche repostero 1 kg|Pack de potes de dulce de leche repostero|Dulces y untables|PACK|6|N|23800",
            "DUL-003|Mermelada de durazno 454 g|Pack de frascos de mermelada de durazno|Dulces y untables|PACK|12|N|19400",
            "DUL-004|Dulce de membrillo 500 g|Pack de panes de dulce de membrillo|Dulces y untables|PACK|12|N|17900",
            "DUL-005|Cacao en polvo 180 g|Pack de paquetes de cacao en polvo|Dulces y untables|PACK|12|N|16300",
            "REP-001|Premezcla bizcochuelo vainilla 500 g|Pack de premezclas para bizcochuelo|Repostería|PACK|12|N|14800",
            "REP-002|Premezcla brownie 500 g|Pack de premezclas para brownie|Repostería|PACK|12|N|16900",
            "REP-003|Chocolate cobertura 1 kg|Caja de barras de chocolate para cobertura|Repostería|CAJA|6|N|38500",
            "REP-004|Grajeas multicolor 100 g|Pack de frascos de grajeas de colores|Repostería|PACK|24|N|11700",
            "REP-005|Esencia de vainilla 100 ml|Pack de frascos de esencia de vainilla|Repostería|PACK|12|N|8400");

    /** Productos con stock particular: cantidad inicial y stock mínimo. Cantidad 0 = sin stock; menor al mínimo = stock bajo. */
    static final Map<String, int[]> STOCK_ESPECIAL = new LinkedHashMap<>();

    static {
        STOCK_ESPECIAL.put("ENE-002", new int[]{0, 30});
        STOCK_ESPECIAL.put("REP-005", new int[]{0, 25});
        STOCK_ESPECIAL.put("GAL-005", new int[]{0, 30});
        STOCK_ESPECIAL.put("SNA-005", new int[]{12, 40});
        STOCK_ESPECIAL.put("CHO-003", new int[]{8, 30});
        STOCK_ESPECIAL.put("GOM-005", new int[]{20, 50});
    }

    // ------------------------------------------------------------------ Geografía

    /** Departamento -> latitud, longitud y zona del preventista (0 a 3). */
    static final Map<String, double[]> DEPARTAMENTOS = new LinkedHashMap<>();

    static {
        DEPARTAMENTOS.put("Capital", new double[]{-32.8895, -68.8458, 0});
        DEPARTAMENTOS.put("Las Heras", new double[]{-32.8517, -68.8283, 0});
        DEPARTAMENTOS.put("Lavalle", new double[]{-32.7220, -68.6070, 0});
        DEPARTAMENTOS.put("Godoy Cruz", new double[]{-32.9268, -68.8433, 1});
        DEPARTAMENTOS.put("Luján de Cuyo", new double[]{-33.0353, -68.8783, 1});
        DEPARTAMENTOS.put("Maipú", new double[]{-32.9783, -68.7900, 1});
        DEPARTAMENTOS.put("Guaymallén", new double[]{-32.8995, -68.7770, 2});
        DEPARTAMENTOS.put("San Martín", new double[]{-33.0806, -68.4681, 2});
        DEPARTAMENTOS.put("Junín", new double[]{-33.1500, -68.4900, 2});
        DEPARTAMENTOS.put("Rivadavia", new double[]{-33.1883, -68.4651, 2});
        DEPARTAMENTOS.put("Santa Rosa", new double[]{-33.2500, -68.1578, 2});
        DEPARTAMENTOS.put("La Paz", new double[]{-33.4600, -67.5500, 2});
        DEPARTAMENTOS.put("Tunuyán", new double[]{-33.5667, -69.0167, 3});
        DEPARTAMENTOS.put("Tupungato", new double[]{-33.3667, -69.1500, 3});
        DEPARTAMENTOS.put("San Carlos", new double[]{-33.7667, -69.0500, 3});
        DEPARTAMENTOS.put("San Rafael", new double[]{-34.6177, -68.3301, 3});
        DEPARTAMENTOS.put("General Alvear", new double[]{-34.9800, -67.6900, 3});
        DEPARTAMENTOS.put("Malargüe", new double[]{-35.4750, -69.5850, 3});
    }

    // ------------------------------------------------------------------ Empleados

    static final String ADMINISTRATIVO = "Lucía|Fernández|28111222|ADM-001|Calle Colón|575|Capital";

    /** nombre|apellido|dni|legajo|zona|calle|numero|departamento. El índice de la lista es la zona (0 a 3). */
    static final List<String> PREVENTISTAS = List.of(
            "Martín|Giménez|27345678|PREV-001|Gran Mendoza Norte|Calle Belgrano|820|Las Heras",
            "Sofía|Herrera|29456789|PREV-002|Gran Mendoza Sur|Calle Perú|455|Godoy Cruz",
            "Diego|Quiroga|31567890|PREV-003|Este provincial|Calle Mitre|1310|Guaymallén",
            "Valeria|Ortiz|32678901|PREV-004|Valle de Uco y Sur|Calle Moreno|290|Tunuyán");

    /** nombre|apellido|dni|legajo|vehiculo|patente|calle|numero|departamento. */
    static final List<String> DISTRIBUIDORES = List.of(
            "Raúl|Benítez|33789012|DIST-001|Utilitario Kangoo|AB123CD|Calle Sarmiento|640|Capital",
            "Pablo|Sosa|34890123|DIST-002|Furgón Fiorino|AC456EF|Calle Rivadavia|1180|Guaymallén",
            "Nicolás|Ruiz|35901234|DIST-003|Camión liviano Daily|AD789GH|Calle Alem|370|San Rafael");

    /** Distribuidor (índice de DISTRIBUIDORES) que cubre cada zona de preventista. */
    static final int[] DISTRIBUIDOR_POR_ZONA = {0, 0, 1, 2};

    // ------------------------------------------------------------------ Clientes y comercios

    /** nombre|apellido. El documento y el correo se generan a partir del número de orden. */
    static final List<String> CLIENTES = List.of(
            "Ricardo|Gómez", "Marta|Pérez", "Hugo|Sánchez", "Laura|Díaz", "Carlos|Romero", "Silvia|Torres",
            "Jorge|Álvarez", "Patricia|Ruiz", "Fernando|Castro", "Gabriela|Molina", "Alberto|Medina",
            "Claudia|Suárez", "Oscar|Benavidez", "Mónica|Rojas", "Daniel|Vega", "Liliana|Acosta",
            "Sergio|Paredes", "Beatriz|Luna", "Gustavo|Ibáñez", "Elena|Cabrera", "Marcelo|Arias", "Noemí|Funes");

    /** indiceCliente|nombreFantasia|razonSocial|condicionIva(R/M/E)|departamento|calle|numero. */
    static final List<String> COMERCIOS = List.of(
            "0|Kiosco El Sol|Gómez Ricardo|M|Capital|Av. San Martín|1240",
            "1|Almacén Don Pedro|Pérez Marta|M|Capital|Calle Godoy Cruz|310",
            "2|Maxikiosco Central|Sánchez Hugo SRL|R|Capital|Calle Rioja|905",
            "3|Despensa La Esquina|Díaz Laura|M|Godoy Cruz|Calle Tirasso|760",
            "4|Kiosco Los Andes|Romero Carlos|M|Godoy Cruz|Av. San Martín|2150",
            "4|Autoservicio Romero|Romero Carlos|M|Godoy Cruz|Calle Paso de los Andes|480",
            "5|Golosinas Torres|Torres Silvia|M|Guaymallén|Calle Bandera de los Andes|1320",
            "6|Almacén El Progreso|Álvarez Jorge SA|R|Guaymallén|Calle Mathus Hoyos|215",
            "7|Kiosco Ruiz|Ruiz Patricia|E|Guaymallén|Calle Pedro Molina|870",
            "8|Maxikiosco Castro|Castro Fernando|M|Las Heras|Av. San Martín|1590",
            "9|Despensa Molina|Molina Gabriela|M|Las Heras|Calle Alvarado|330",
            "9|Kiosco La Plaza|Molina Gabriela|M|Las Heras|Calle Pueyrredón|120",
            "10|Almacén Medina|Medina Alberto|M|Luján de Cuyo|Calle Italia|640",
            "11|Supermercado Suárez|Suárez Claudia SRL|R|Luján de Cuyo|Calle Ozamis|1010",
            "12|Kiosco Benavidez|Benavidez Oscar|M|Maipú|Calle Pescara|405",
            "13|Autoservicio Rojas|Rojas Mónica|M|Maipú|Calle Urquiza|770",
            "14|Despensa Vega|Vega Daniel|M|Lavalle|Calle Pedro Molina|150",
            "15|Kiosco Acosta|Acosta Liliana|M|San Martín|Calle Mitre|995",
            "16|Maxikiosco Paredes|Paredes Sergio SRL|R|San Martín|Av. España|1280",
            "17|Almacén Luna|Luna Beatriz|M|Junín|Calle Lavalle|235",
            "18|Kiosco Ibáñez|Ibáñez Gustavo|M|Rivadavia|Calle Belgrano|610",
            "19|Despensa Cabrera|Cabrera Elena|M|Santa Rosa|Calle San Martín|340",
            "20|Almacén Arias|Arias Marcelo|M|La Paz|Calle Sarmiento|185",
            "21|Kiosco Funes|Funes Noemí|M|San Rafael|Av. Bartolomé Mitre|1450",
            "21|Supermercado Funes|Funes Noemí SRL|R|San Rafael|Calle Alvarez Condarco|720",
            "1|Mayorista Don Pedro|Pérez Marta|R|General Alvear|Av. Alvear Oeste|830",
            "3|Despensa Malargüe Sur|Díaz Laura|M|Malargüe|Calle Fortín|260",
            "5|Golosinas del Valle|Torres Silvia|M|Tunuyán|Calle Alem|415",
            "7|Kiosco Tupungato|Ruiz Patricia|M|Tupungato|Calle Belgrano|520",
            "10|Almacén San Carlos|Medina Alberto|M|San Carlos|Calle Moreno|175");
}
