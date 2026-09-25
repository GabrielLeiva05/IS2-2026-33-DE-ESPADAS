package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoTelefono;
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
    @Autowired
    private ServicioContactoCorreoElectronico svcContactoCorreoElectronico;
    @Autowired
    private ServicioContactoTelefonico svcContactoTelefonico;

    public void validar(String razonSocial) throws Exception {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new Exception("La razón social es obligatoria");
        }
    }

    @Transactional
    public Proveedor crearProveedor(String razonSocial, String email, String telefonoFijo, String telefonoCelular) throws Exception {
        validar(razonSocial);

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(razonSocial);
        proveedor.setEliminado(false);
        proveedor = repositorio.save(proveedor);

        if (email != null && !email.isBlank()) {
            svcContactoCorreoElectronico.crearContactoCorreoElectronico(email, TipoContacto.EMPRESA, null, null, proveedor.getId());
        }
        if (telefonoFijo != null && !telefonoFijo.isBlank()) {
            svcContactoTelefonico.crearContactoTelefonico(telefonoFijo, TipoTelefono.FIJO, TipoContacto.EMPRESA, null, null, proveedor.getId());
        }
        if (telefonoCelular != null && !telefonoCelular.isBlank()) {
            svcContactoTelefonico.crearContactoTelefonico(telefonoCelular, TipoTelefono.CELULAR, TipoContacto.EMPRESA, null, null, proveedor.getId());
        }

    }

    @Transactional
    public Proveedor modificarProveedor(String id, String razonSocial, String email, String telefonoWhatsapp) throws Exception {
        Proveedor proveedor = buscarProveedor(id);
        validar(razonSocial, email);
        proveedor.setRazonSocial(razonSocial);

        return repositorio.save(proveedor);
    }

    @Transactional
    public void eliminarProveedor(String id) throws Exception {
        Proveedor proveedor = buscarProveedor(id);
        proveedor.setEliminado(true);
        repositorio.save(proveedor);
    }

    public Proveedor buscarProveedor(String id) throws Exception {
        return repositorio.findById(id)
                .orElseThrow(() -> new Exception("No existe el proveedor con id " + id));
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
