package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.Cliente;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;

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
        return repository.save(cliente);
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
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        validar(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento);
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setFechaNacimiento(fechaNacimiento);
        cliente.setTipoDocumento(tipoDocumento);
        cliente.setDocumento(numeroDocumento);
        cliente.setDireccionEstadia(direccionEstadia);
        return repository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarCliente() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClienteActivo() {
        return repository.findByEliminadoFalse();
    }
    
    // public void asociarClienteUsuario(Cliente cliente, Usuario usuario) { ... }
}