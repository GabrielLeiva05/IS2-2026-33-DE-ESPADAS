package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.Sucursal;
import com.ejercicioIntegrador.tiendaderopa.repository.SucursalRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class ServicioSucursal {

    private final SucursalRepositorio repositorio;

    public ServicioSucursal(SucursalRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public Sucursal obtenerPrincipal() {
        return repositorio.findFirstByActivaTrueAndPrincipalTrueOrderByNombreAsc()
                .orElseThrow(() -> new IllegalStateException("No hay una sucursal principal activa"));
    }

    @Transactional(readOnly = true)
    public Sucursal buscarActiva(String id) {
        return repositorio.findById(id)
                .filter(Sucursal::isActiva)
                .orElseThrow(() -> new IllegalArgumentException("No existe una sucursal activa con ese ID"));
    }

    @Transactional
    public Sucursal crear(String nombre, String direccion, boolean principal) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la sucursal es obligatorio");
        }
        String nombreNormalizado = nombre.trim();
        if (repositorio.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new IllegalArgumentException("Ya existe una sucursal con ese nombre");
        }
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre(nombreNormalizado);
        sucursal.setDireccion(direccion == null || direccion.isBlank() ? null : direccion.trim());
        sucursal.setActiva(true);
        sucursal.setPrincipal(principal);
        if (principal) {
            repositorio.findFirstByActivaTrueAndPrincipalTrueOrderByNombreAsc()
                    .ifPresent(actual -> actual.setPrincipal(false));
        }
        return repositorio.save(sucursal);
    }
}