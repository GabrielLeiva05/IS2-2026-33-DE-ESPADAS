package com.dulcemayor.admin.dto;

import java.util.List;

/**
 * Datos del panel de inicio. Cada lista puede venir vacía según el rol: el panel solo muestra
 * lo que la API autoriza a ese rol.
 */
public record PanelDTO(List<IndicadorDTO> indicadores, List<SegmentoDTO> categorias, String fondoDona,
                       long totalPublicados, List<StockDTO> stockCritico, List<ConteoRolDTO> personal) {

    public List<IndicadorDTO> getIndicadores() { return indicadores; }
    public List<SegmentoDTO> getCategorias() { return categorias; }
    public String getFondoDona() { return fondoDona; }
    public long getTotalPublicados() { return totalPublicados; }
    public List<StockDTO> getStockCritico() { return stockCritico; }
    public List<ConteoRolDTO> getPersonal() { return personal; }
}
