package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.Cliente;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Transactional
    public Cliente crearCliente(String nombre, String apellido, Date fechaNacimiento, 
                                TipoDocumento tipoDocumento, String numeroDocumento, String direccionEstadia) {
        validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento);
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setFechaNacimiento(fechaNacimiento);
        cliente.setTipoDocumento(tipoDocumento);
        cliente.setDocumento(numeroDocumento);
        cliente.setDireccionEstadia(direccionEstadia);
        return clienteRepository.save(cliente);
    }

    public void validar(String nombre, String apellido, Date fechaNacimiento, 
                        TipoDocumento tipoDocumento, String numeroDocumento) {
        if (nombre == null || apellido == null || numeroDocumento == null) {
            throw new IllegalArgumentException("Campos obligatorios incompletos");
        }
    }

    @Transactional
    public Cliente modificarCliente(String id, String nombre, String apellido, 
                                    Date fechaNacimiento, TipoDocumento tipoDocumento, 
                                    String numeroDocumento, String direccionEstadia) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento);
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setFechaNacimiento(fechaNacimiento);
        cliente.setTipoDocumento(tipoDocumento);
        cliente.setDocumento(numeroDocumento);
        cliente.setDireccionEstadia(direccionEstadia);
        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarCliente() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClienteActivo() {
        return clienteRepository.findByEliminadoFalse();
    }
    
    @Transactional
    public void asociarClienteUsuario(String clienteId, String usuarioId) throws MiException {
        // 1. Buscar entidades utilizando sus respectivos servicios / repositorios
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new MiException("Cliente no encontrado con id: " + clienteId));

        Usuario nuevoUsuario = usuarioServicio.buscarPorId(usuarioId);

        // 2. Validar que el usuario no esté asociado a otra persona distinta
        if (nuevoUsuario.getPersona() != null && !nuevoUsuario.getPersona().getId().equals(clienteId)) {
            throw new MiException("El usuario ya está asociado a otra persona");
        }

        // 3. Delegar la desactivación del usuario anterior al servicio de Usuario
        usuarioServicio.desactivarUsuarioActivoDePersona(cliente.getId(), nuevoUsuario.getId());

        // 4. Delegar la asociación al servicio de Usuario
        usuarioServicio.asociarAPersona(nuevoUsuario, cliente);

        // 5. Mantener la consistencia en el objeto cliente
        if (!cliente.getUsuarios().contains(nuevoUsuario)) {
            cliente.getUsuarios().add(nuevoUsuario);
        }

        clienteRepository.save(cliente);
    }
}