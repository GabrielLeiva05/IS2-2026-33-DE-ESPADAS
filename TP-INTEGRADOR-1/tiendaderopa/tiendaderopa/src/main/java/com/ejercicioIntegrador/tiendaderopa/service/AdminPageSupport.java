package com.ejercicioIntegrador.tiendaderopa.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.ui.Model;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class AdminPageSupport {

    private AdminPageSupport() {
    }

    public static void cargar(Model model, String titulo, String basePath, Class<?> tipo,
            Collection<?> registros, List<Map<String, Object>> campos, Object seleccionado) {
        List<PropertyDescriptor> propiedades = propiedadesSimples(tipo);
        List<Map<String, String>> columnas = propiedades.stream()
                .map(propiedad -> Map.of("clave", propiedad.getName(), "etiqueta", etiqueta(propiedad.getName())))
                .toList();
        List<Map<String, Object>> filas = registros.stream()
                .map(registro -> fila(registro, propiedades))
                .toList();

        model.addAttribute("titulo", titulo);
        model.addAttribute("basePath", basePath);
        model.addAttribute("detallePath", basePath);
        model.addAttribute("columnas", columnas);
        model.addAttribute("filas", filas);
        model.addAttribute("campos", campos);
        model.addAttribute("valores", valores(seleccionado, campos));
        model.addAttribute("accion", seleccionado == null ? basePath : basePath + "/" + id(seleccionado));
        model.addAttribute("modoEdicion", seleccionado != null);
        model.addAttribute("permitirCrear", true);
        model.addAttribute("permitirEditar", true);
        model.addAttribute("permitirEliminar", true);
        model.addAttribute("asociacionUsuarios", false);
        model.addAttribute("desasociarUsuarios", false);
        model.addAttribute("permitirVerDetalle", false);
        model.addAttribute("permitirAnular", false);
        model.addAttribute("accionSecundaria", null);
        model.addAttribute("camposSecundarios", List.of());
        model.addAttribute("estadosOrden", List.of());
        model.addAttribute("permitirCambiarEstado", false);
        model.addAttribute("permitirFiltrarEstado", false);
        model.addAttribute("estadoSeleccionado", "");
        model.addAttribute("permitirCrearOrden", false);
        model.addAttribute("permitirAgregarDetalle", false);
        model.addAttribute("permitirFiltroId", false);
        model.addAttribute("rutaFiltroId", basePath);
        model.addAttribute("nombreFiltroId", "id");
        model.addAttribute("permitirBuscarNombre", false);
        model.addAttribute("permitirMostrarActivos", false);
        model.addAttribute("usuariosDisponibles", Map.of());
        model.addAttribute("productosDisponibles", Map.of());
    }

    public static Map<String, Object> campo(String nombre, String tipo, boolean requerido, String... opciones) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (String opcion : opciones) {
            mapa.put(opcion, opcion);
        }
        return campoConOpciones(nombre, tipo, requerido, mapa);
    }

    /** Campo de selección para una relación (FK): el value es el id real, el texto es la etiqueta legible. */
    public static Map<String, Object> campoRelacion(String nombre, boolean requerido, Map<String, String> opciones) {
        return campoConOpciones(nombre, "select", requerido, opciones);
    }

    /** Arma un mapa id -> etiqueta a partir de una colección de entidades, para usar en campoRelacion. */
    public static <T> Map<String, String> mapaOpciones(Collection<T> entidades, Function<T, String> idFn,
            Function<T, String> etiquetaFn) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (T entidad : entidades) {
            mapa.put(idFn.apply(entidad), etiquetaFn.apply(entidad));
        }
        return mapa;
    }

    private static Map<String, Object> campoConOpciones(String nombre, String tipo, boolean requerido,
            Map<String, String> opciones) {
        Map<String, Object> campo = new LinkedHashMap<>();
        campo.put("nombre", nombre);
        campo.put("etiqueta", etiqueta(nombre));
        campo.put("tipo", tipo);
        campo.put("requerido", requerido);
        campo.put("opcionesMapa", opciones);
        return campo;
    }

    public static String id(Object entidad) {
        if (entidad == null) {
            return "";
        }
        Object valor = new BeanWrapperImpl(entidad).getPropertyValue("id");
        return valor == null ? "" : valor.toString();
    }

    private static List<PropertyDescriptor> propiedadesSimples(Class<?> tipo) {
        try {
            return java.util.Arrays.stream(Introspector.getBeanInfo(tipo).getPropertyDescriptors())
                    .filter(propiedad -> !"class".equals(propiedad.getName()))
                    .filter(propiedad -> propiedad.getReadMethod() != null)
                    .filter(propiedad -> BeanUtils.isSimpleValueType(propiedad.getPropertyType()))
                    .toList();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron inspeccionar las propiedades de " + tipo.getSimpleName(), ex);
        }
    }

    private static Map<String, Object> fila(Object entidad, List<PropertyDescriptor> propiedades) {
        BeanWrapper bean = new BeanWrapperImpl(entidad);
        Map<String, Object> fila = new LinkedHashMap<>();
        for (PropertyDescriptor propiedad : propiedades) {
            Object valor = bean.getPropertyValue(propiedad.getName());
            fila.put(propiedad.getName(), mostrar(valor));
        }
        fila.put("id", id(entidad));
        return fila;
    }

    private static Map<String, Object> valores(Object seleccionado, List<Map<String, Object>> campos) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        if (seleccionado == null) {
            return resultado;
        }
        BeanWrapper bean = new BeanWrapperImpl(seleccionado);
        for (Map<String, Object> campo : campos) {
            String nombre = (String) campo.get("nombre");
            if (bean.isReadableProperty(nombre)) {
                resultado.put(nombre, mostrar(bean.getPropertyValue(nombre)));
            }
        }
        return resultado;
    }

    private static String mostrar(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof Date fecha) {
            return new SimpleDateFormat("yyyy-MM-dd").format(fecha);
        }
        if (valor instanceof TemporalAccessor || valor instanceof Enum<?> || valor instanceof CharSequence
                || valor instanceof Number || valor instanceof Boolean) {
            return valor.toString();
        }
        // Entidad relacionada (FK): se muestra/compara por su id, no por Object#toString().
        BeanWrapper bean = new BeanWrapperImpl(valor);
        if (bean.isReadableProperty("id")) {
            Object id = bean.getPropertyValue("id");
            return id == null ? "" : id.toString();
        }
        return valor.toString();
    }

    private static String etiqueta(String nombre) {
        String conEspacios = nombre.replaceAll("([a-z])([A-Z])", "$1 $2");
        return Character.toUpperCase(conEspacios.charAt(0)) + conEspacios.substring(1);
    }
}