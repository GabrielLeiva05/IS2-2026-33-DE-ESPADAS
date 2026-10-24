package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.ObjetivoReposicion;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.Sucursal;
import com.ejercicioIntegrador.tiendaderopa.repository.ObjetivoReposicionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioObjetivoReposicion {

    private final ObjetivoReposicionRepositorio repositorio;
    private final ServicioSucursal servicioSucursal;
    private final ServicioProducto servicioProducto;
    private final ServicioStock servicioStock;

    public ServicioObjetivoReposicion(ObjetivoReposicionRepositorio repositorio,
            ServicioSucursal servicioSucursal, ServicioProducto servicioProducto, ServicioStock servicioStock) {
        this.repositorio = repositorio;
        this.servicioSucursal = servicioSucursal;
        this.servicioProducto = servicioProducto;
        this.servicioStock = servicioStock;
    }

    @Transactional
    public ObjetivoReposicion configurar(String sucursalId, String productoId, int cantidadObjetivo) {
        if (cantidadObjetivo < 0) {
            throw new IllegalArgumentException("El objetivo de reposición no puede ser negativo");
        }
        Sucursal sucursal = servicioSucursal.buscarActiva(sucursalId);
        Producto producto = servicioProducto.buscarPorId(productoId);
        ObjetivoReposicion objetivo = repositorio.bloquearPorSucursalYProducto(sucursalId, productoId)
                .orElseGet(() -> {
                    ObjetivoReposicion nuevo = new ObjetivoReposicion();
                    nuevo.setCantidadActual(servicioStock.cantidadActual(productoId, sucursalId));
                    return nuevo;
                });
        objetivo.setSucursal(sucursal);
        objetivo.setProducto(producto);
        objetivo.setCantidadObjetivo(cantidadObjetivo);
        return repositorio.save(objetivo);
    }

    @Transactional(readOnly = true)
    public ObjetivoReposicion buscar(String sucursalId, String productoId) {
        return repositorio.findBySucursal_IdAndProducto_Id(sucursalId, productoId)
                .orElseThrow(() -> new IllegalArgumentException("No hay objetivo configurado para el producto en la sucursal"));
    }

    @Transactional(readOnly = true)
    public int cantidadAReponer(String sucursalId, String productoId) {
        ObjetivoReposicion objetivo = buscar(sucursalId, productoId);
        return Math.max(0, objetivo.getCantidadObjetivo() - objetivo.getCantidadActual());
    }

    @Transactional(readOnly = true)
    public int obtenerCantidadObjetivo(String sucursalId, String productoId, int valorAlternativo) {
        return repositorio.findBySucursal_IdAndProducto_Id(sucursalId, productoId)
                .map(ObjetivoReposicion::getCantidadObjetivo)
                .orElse(Math.max(valorAlternativo, 0));
    }
}