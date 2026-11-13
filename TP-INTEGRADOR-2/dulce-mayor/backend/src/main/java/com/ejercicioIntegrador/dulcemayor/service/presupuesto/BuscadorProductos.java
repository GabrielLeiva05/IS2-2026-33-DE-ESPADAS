package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

import com.ejercicioIntegrador.dulcemayor.dto.ProductoCatalogoDTO;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Busca en el catálogo el producto que nombró el cliente. Compara palabras sin tildes ni plurales contra el nombre,
 * la categoría y la subcategoría; un código exacto gana siempre. Si varios empatan, el resultado es ambiguo y el
 * asistente pregunta cuál.
 */
public final class BuscadorProductos {

    /** Proporción mínima de palabras del pedido que tienen que aparecer en el producto. */
    static final double MINIMO = 0.5;
    private static final Set<String> IGNORADAS = Set.of("de", "del", "la", "el", "los", "las", "con", "y", "x", "un",
            "una", "unos", "unas", "caja", "cajas", "pack", "packs", "bulto", "bultos", "unidad", "unidades", "sabor");

    public record Resultado(List<ProductoCatalogoDTO> candidatos) {

        public boolean encontrado() {
            return candidatos.size() == 1;
        }

        public boolean ambiguo() {
            return candidatos.size() > 1;
        }

        public ProductoCatalogoDTO unico() {
            return candidatos.get(0);
        }
    }

    private BuscadorProductos() {
    }

    public static Resultado buscar(String texto, List<ProductoCatalogoDTO> catalogo) {
        String normalizado = normalizar(texto);
        for (ProductoCatalogoDTO producto : catalogo) {
            if (producto.codigo() != null && normalizar(producto.codigo()).equals(normalizado)) {
                return new Resultado(List.of(producto));
            }
        }
        List<String> buscadas = palabras(texto);
        if (buscadas.isEmpty()) {
            return new Resultado(List.of());
        }
        List<Puntaje> puntajes = new ArrayList<>();
        for (ProductoCatalogoDTO producto : catalogo) {
            double puntaje = puntaje(buscadas, producto);
            if (puntaje >= MINIMO) {
                puntajes.add(new Puntaje(producto, puntaje, normalizar(producto.nombre()).equals(normalizado)));
            }
        }
        if (puntajes.isEmpty()) {
            return new Resultado(List.of());
        }
        puntajes.sort(Comparator.comparingDouble(Puntaje::valor).reversed()
                .thenComparing(p -> p.producto().nombre(), String.CASE_INSENSITIVE_ORDER));
        for (Puntaje p : puntajes) {
            if (p.nombreExacto()) {
                return new Resultado(List.of(p.producto()));
            }
        }
        double mejor = puntajes.get(0).valor();
        return new Resultado(puntajes.stream().filter(p -> p.valor() == mejor).limit(5)
                .map(Puntaje::producto).toList());
    }

    /** Productos que coinciden con un rubro ("chocolates") por categoría, subcategoría o nombre. */
    public static List<ProductoCatalogoDTO> delRubro(String rubro, List<ProductoCatalogoDTO> catalogo) {
        List<String> buscadas = palabras(rubro);
        if (buscadas.isEmpty()) {
            return catalogo;
        }
        return catalogo.stream().filter(p -> puntaje(buscadas, p) >= MINIMO).toList();
    }

    private static double puntaje(List<String> buscadas, ProductoCatalogoDTO producto) {
        Set<String> propias = new LinkedHashSet<>(palabras(producto.nombre()));
        propias.addAll(palabras(producto.categoria()));
        propias.addAll(palabras(producto.subCategoria()));
        int halladas = 0;
        for (String buscada : buscadas) {
            if (propias.stream().anyMatch(p -> p.equals(buscada) || (buscada.length() >= 4 && p.startsWith(buscada)))) {
                halladas++;
            }
        }
        return (double) halladas / buscadas.size();
    }

    static List<String> palabras(String texto) {
        List<String> resultado = new ArrayList<>();
        for (String palabra : normalizar(texto).split(" ")) {
            if (!palabra.isEmpty() && !IGNORADAS.contains(palabra)) {
                resultado.add(singular(palabra));
            }
        }
        return resultado;
    }

    /** alfajores → alfajor, chicles → chicle, turrones → turron (aproximado, alcanza para comparar). */
    static String singular(String palabra) {
        if (palabra.length() > 4 && palabra.endsWith("es") && !palabra.endsWith("ees")
                && "rnsdz".indexOf(palabra.charAt(palabra.length() - 3)) >= 0) {
            return palabra.substring(0, palabra.length() - 2);
        }
        if (palabra.length() > 3 && palabra.endsWith("s")) {
            return palabra.substring(0, palabra.length() - 1);
        }
        return palabra;
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private record Puntaje(ProductoCatalogoDTO producto, double valor, boolean nombreExacto) {
    }
}
