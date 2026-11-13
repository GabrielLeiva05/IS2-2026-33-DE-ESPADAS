package com.dulcemayor.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Formulario de alta y edición de promociones. Los tramos llegan como listas paralelas. */
@Getter
@Setter
@NoArgsConstructor
public class PromocionFormularioDTO {

    private String id;
    private String tipo = "PORCENTAJE_PRODUCTO";
    private String nombre;
    private String descripcion;
    private String fechaInicio;
    private String fechaFin;
    private boolean activa = true;
    private List<String> idsProductos = new ArrayList<>();
    private List<String> idsSubCategorias = new ArrayList<>();
    private BigDecimal porcentaje;
    private List<Integer> tramoDesde = new ArrayList<>();
    private List<Integer> tramoHasta = new ArrayList<>();
    private List<BigDecimal> tramoPorcentaje = new ArrayList<>();
    private String codigo;
    private String tipoValor = "PORCENTAJE";
    private BigDecimal valor;
    private BigDecimal montoMinimo;
    private Integer usosMaximos;
    private boolean acumulable = true;

    public boolean isNuevo() {
        return id == null || id.isBlank();
    }

    /** Cantidad de tramos a dibujar (al menos uno). */
    public int getTramos() {
        return Math.max(1, tramoDesde.size());
    }

    public Integer desde(int i) {
        return i < tramoDesde.size() ? tramoDesde.get(i) : null;
    }

    public Integer hasta(int i) {
        return i < tramoHasta.size() ? tramoHasta.get(i) : null;
    }

    public BigDecimal porcentajeTramo(int i) {
        return i < tramoPorcentaje.size() ? tramoPorcentaje.get(i) : null;
    }

    public static PromocionFormularioDTO nuevo(String tipo) {
        PromocionFormularioDTO f = new PromocionFormularioDTO();
        if (tipo != null && List.of("PORCENTAJE_PRODUCTO", "VOLUMEN", "CODIGO").contains(tipo)) {
            f.setTipo(tipo);
        }
        f.setFechaInicio(LocalDate.now().toString());
        f.setFechaFin(LocalDate.now().plusDays(30).toString());
        return f;
    }

    public static PromocionFormularioDTO desde(PromocionDTO p) {
        PromocionFormularioDTO f = new PromocionFormularioDTO();
        f.setId(p.getId());
        f.setTipo(p.getTipo());
        f.setNombre(p.getNombre());
        f.setDescripcion(p.getDescripcion());
        f.setFechaInicio(p.getFechaInicio() == null ? null : p.getFechaInicio().toString());
        f.setFechaFin(p.getFechaFin() == null ? null : p.getFechaFin().toString());
        f.setActiva(p.isActiva());
        p.getProductos().forEach(r -> f.getIdsProductos().add(r.getId()));
        p.getSubCategorias().forEach(r -> f.getIdsSubCategorias().add(r.getId()));
        f.setPorcentaje(p.getPorcentaje());
        for (PromocionTramoDTO t : p.getTramos()) {
            f.getTramoDesde().add(t.getCantidadDesde());
            f.getTramoHasta().add(t.getCantidadHasta());
            f.getTramoPorcentaje().add(t.getPorcentaje());
        }
        f.setCodigo(p.getCodigo());
        if (p.getTipoValor() != null) {
            f.setTipoValor(p.getTipoValor());
        }
        f.setValor(p.getValor());
        f.setMontoMinimo(p.getMontoMinimo());
        f.setUsosMaximos(p.getUsosMaximos());
        f.setAcumulable(p.isAcumulable());
        return f;
    }
}
