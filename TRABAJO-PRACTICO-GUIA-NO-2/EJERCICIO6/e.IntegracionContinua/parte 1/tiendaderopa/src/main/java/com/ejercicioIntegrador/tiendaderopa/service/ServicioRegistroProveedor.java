package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoTelefono;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de CASO DE USO (no de una entidad puntual): orquesta el alta
 * completa de un Proveedor junto con sus contactos, que es la regla de
 * negocio real ("al registrar un proveedor, se cargan sus datos de
 * contacto en el mismo trámite").
 *
 * Depende de ServicioProveedor, ServicioContactoCorreoElectronico y
 * ServicioContactoTelefonico — pero ninguno de esos tres depende de
 * este. Así se evita el ciclo que había antes, sin sacar la regla de
 * negocio del Service ni moverla al Controller.
 */
@Service
public class ServicioRegistroProveedor {

    @Autowired
    private ServicioProveedor svcProveedor;
    @Autowired
    private ServicioContactoCorreoElectronico svcContactoCorreoElectronico;
    @Autowired
    private ServicioContactoTelefonico svcContactoTelefonico;

    @Transactional
    public Proveedor registrarProveedor(String razonSocial, String email,
                                        String telefonoFijo, String telefonoCelular) throws Exception {
        Proveedor proveedor = svcProveedor.crearProveedor(razonSocial);

        if (email != null && !email.isBlank()) {
            svcContactoCorreoElectronico.crearContactoCorreoElectronico(
                    email, TipoContacto.EMPRESA, null, null, proveedor.getId());
        }
        if (telefonoFijo != null && !telefonoFijo.isBlank()) {
            svcContactoTelefonico.crearContactoTelefonico(
                    telefonoFijo, TipoTelefono.FIJO, TipoContacto.EMPRESA, null, null, proveedor.getId());
        }
        if (telefonoCelular != null && !telefonoCelular.isBlank()) {
            svcContactoTelefonico.crearContactoTelefonico(
                    telefonoCelular, TipoTelefono.CELULAR, TipoContacto.EMPRESA, null, null, proveedor.getId());
        }

        return proveedor;
    }
}