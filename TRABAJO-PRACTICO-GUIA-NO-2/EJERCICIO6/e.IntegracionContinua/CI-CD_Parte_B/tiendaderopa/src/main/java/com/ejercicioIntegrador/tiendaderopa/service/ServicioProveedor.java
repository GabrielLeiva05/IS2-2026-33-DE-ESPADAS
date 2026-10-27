package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioProveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * ARCHIVO NUEVO. Mismo patrón de ABM que ServicioCategoria, pero con los
 * atributos reales de una empresa (no los de Persona/Cliente).
 */
@Service
public class ServicioProveedor {

    @Autowired
    private RepositorioProveedor repositorio;
    // Sin @Autowired hacia ServicioContactoCorreoElectronico ni
    // ServicioContactoTelefonico: esto es lo que rompe el ciclo desde
    // este lado.

    public void validar(String razonSocial) throws MiException {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new MiException("La razón social es obligatoria");
        }
    }

    @Transactional
    public Proveedor crearProveedor(String razonSocial) throws MiException {
        validar(razonSocial);
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(razonSocial);
        proveedor.setEliminado(false);
        return repositorio.save(proveedor);
    }

    @Transactional
    public Proveedor modificarProveedor(String id, String razonSocial) throws MiException {
        Proveedor proveedor = buscarProveedor(id);
        validar(razonSocial);
        proveedor.setRazonSocial(razonSocial);
        return repositorio.save(proveedor);
    }

    @Transactional
    public void eliminarProveedor(String id) throws MiException {
        Proveedor proveedor = buscarProveedor(id);
        proveedor.setEliminado(true);
        repositorio.save(proveedor);
    }

    public Proveedor buscarProveedor(String id) throws MiException {
        return repositorio.findById(id)
                .orElseThrow(() -> new MiException("No existe el proveedor con id " + id));
    }

    public Proveedor buscarProveedorPorNombre(String razonSocial) {
        return repositorio.findByRazonSocial(razonSocial);
    }

    public Collection<Proveedor> listarProveedor() {
        return repositorio.findAll();
    }

    public Collection<Proveedor> listarProveedorActivo() {
        return repositorio.findByEliminadoFalse();
    }
}