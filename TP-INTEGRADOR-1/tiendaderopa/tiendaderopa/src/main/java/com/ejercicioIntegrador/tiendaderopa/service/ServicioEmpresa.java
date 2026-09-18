package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoSucursal;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Empresa;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioEmpresa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * Todas las reglas de negocio de Empresa viven acá (en el service).
 * La entidad Empresa es solo persistencia, el controller solo orquesta HTTP.
 *
 * NOTA de diseño: el diagrama muestra crearEmpresa/modificarEmpresa recibiendo
 * un "Contacto". En el modelo actual, Contacto.persona es @ManyToOne
 * obligatorio (nullable = false) apuntando solo a Persona, es decir que hoy
 * un Contacto no puede pertenecer a una Empresa sin modificar esa entidad
 * (agregar una columna empresa_id opcional). Como no fue parte de lo pedido,
 * no toqué Contacto/Persona; dejo este service listo para, cuando se decida
 * extender Contacto, resolver esa asociación acá.
 */
@Service
public class ServicioEmpresa {

    @Autowired
    private RepositorioEmpresa repositorio;

    @Transactional
    public Empresa crearEmpresa(String razonSocial, String cuit, TipoSucursal tipoSucursal) throws MiException {

        validar(razonSocial, cuit, tipoSucursal);

        String cuitNormalizado = cuit.trim().replace("-", "");

        if (this.repositorio.existsByCuit(cuitNormalizado)) {
            throw new MiException("Ya existe una empresa registrada con el CUIT: " + cuitNormalizado);
        }

        Empresa empresa = new Empresa(razonSocial.trim(), cuitNormalizado, tipoSucursal);
        return this.repositorio.save(empresa);
    }

    public void validar(String razonSocial, String cuit, TipoSucursal tipoSucursal) throws MiException {
        if (razonSocial == null || razonSocial.trim().isEmpty()) {
            throw new MiException("La razón social no puede estar vacía");
        }
        if (razonSocial.trim().length() < 3) {
            throw new MiException("La razón social debe tener al menos 3 caracteres");
        }
        if (cuit == null || cuit.trim().isEmpty()) {
            throw new MiException("El CUIT no puede estar vacío");
        }
        if (!cuit.trim().replace("-", "").matches("\\d{11}")) {
            throw new MiException("El CUIT debe tener 11 dígitos numéricos (con o sin guiones)");
        }
        if (tipoSucursal == null) {
            throw new MiException("Debe indicar el tipo de sucursal (SEDE_CENTRAL o SUCURSAL)");
        }
    }

    @Transactional
    public Empresa buscarEmpresa(String id) throws MiException {
        return this.repositorio.findById(id)
                .orElseThrow(() -> new MiException("No existe una empresa con id: " + id));
    }

    @Transactional
    public Empresa buscarEmpresaPorNombre(String razonSocial) throws MiException {
        if (razonSocial == null || razonSocial.trim().isEmpty()) {
            throw new MiException("Debe indicar una razón social para buscar");
        }
        return this.repositorio.findByRazonSocialIgnoreCase(razonSocial.trim())
                .orElseThrow(() -> new MiException("No existe una empresa con razón social: " + razonSocial));
    }

    @Transactional
    public Empresa modificarEmpresa(String id, String razonSocial, String cuit, TipoSucursal tipoSucursal) throws MiException {

        validar(razonSocial, cuit, tipoSucursal);

        Empresa empresa = buscarEmpresa(id);

        String cuitNormalizado = cuit.trim().replace("-", "");

        if (!empresa.getCuit().equals(cuitNormalizado) && this.repositorio.existsByCuit(cuitNormalizado)) {
            throw new MiException("Ya existe otra empresa registrada con el CUIT: " + cuitNormalizado);
        }

        empresa.setRazonSocial(razonSocial.trim());
        empresa.setCuit(cuitNormalizado);
        empresa.setTipoSucursal(tipoSucursal);

        return this.repositorio.save(empresa);
    }

    @Transactional
    public void eliminarEmpresa(String id) throws MiException {
        Empresa empresa = buscarEmpresa(id);
        // baja lógica
        empresa.setEliminado(true);
        this.repositorio.save(empresa);
    }

    @Transactional
    public List<Empresa> listarEmpresa() {
        return this.repositorio.findAll();
    }

    @Transactional
    public List<Empresa> listarEmpresaActiva() {
        return this.repositorio.findByEliminadoFalse();
    }
}