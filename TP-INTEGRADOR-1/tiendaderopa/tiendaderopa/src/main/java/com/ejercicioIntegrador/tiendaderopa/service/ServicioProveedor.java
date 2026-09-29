package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioProveedor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioProveedor {

    private final RepositorioProveedor repositorio;

    public ServicioProveedor(RepositorioProveedor repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarTodos() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return repositorio.findByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public Proveedor buscarPorId(String id) throws MiException {
        return repositorio.findById(id)
                .orElseThrow(() -> new MiException("No se encontró el proveedor solicitado"));
    }

    @Transactional
    public Proveedor crearProveedor(String razonSocial) throws MiException {
        validar(razonSocial);
        return repositorio.save(new Proveedor(razonSocial.trim()));
    }

    @Transactional
    public void modificarProveedor(String id, String razonSocial) throws MiException {
        validar(razonSocial);
        Proveedor proveedor = buscarPorId(id);
        proveedor.setRazonSocial(razonSocial.trim());
        repositorio.save(proveedor);
    }

    @Transactional
    public void eliminar(String id) throws MiException {
        Proveedor proveedor = buscarPorId(id);
        proveedor.setEliminado(true); // baja lógica
        repositorio.save(proveedor);
    }

    private void validar(String razonSocial) throws MiException {
        if (razonSocial == null || razonSocial.trim().isEmpty()) {
            throw new MiException("La razón social del proveedor no puede estar vacía");
        }
        if (razonSocial.trim().length() < 3) {
            throw new MiException("La razón social debe tener al menos 3 caracteres");
        }
    }
}
