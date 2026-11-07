package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.SucursalRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SucursalResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Sucursal;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioSucursal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Sucursales: las administra el personal administrativo y las consulta el sitio público. */
@Service
public class ServicioSucursal {

    private final RepositorioSucursal repositorioSucursal;
    private final ServicioDireccion servicioDireccion;

    public ServicioSucursal(RepositorioSucursal repositorioSucursal, ServicioDireccion servicioDireccion) {
        this.repositorioSucursal = repositorioSucursal;
        this.servicioDireccion = servicioDireccion;
    }

    @Transactional(readOnly = true)
    public List<SucursalResponseDTO> listarActivas() {
        return repositorioSucursal.findByActivaTrueAndEliminadoFalseOrderByNombreAsc().stream()
                .map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public List<SucursalResponseDTO> listarTodas() {
        return repositorioSucursal.findByEliminadoFalse().stream().map(this::convertir).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public SucursalResponseDTO alta(SucursalRequestDTO dto) throws MiException {
        validar(dto);
        Sucursal sucursal = new Sucursal();
        sucursal.setDireccion(servicioDireccion.crearDireccion(dto.direccion()));
        copiar(sucursal, dto);
        return convertir(repositorioSucursal.save(sucursal));
    }

    @Transactional(rollbackFor = Exception.class)
    public SucursalResponseDTO modificar(String id, SucursalRequestDTO dto) throws MiException {
        validar(dto);
        Sucursal sucursal = repositorioSucursal.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new MiException("No existe la sucursal indicada"));
        copiar(sucursal, dto);
        return convertir(repositorioSucursal.save(sucursal));
    }

    @Transactional(rollbackFor = Exception.class)
    public void baja(String id) throws MiException {
        Sucursal sucursal = repositorioSucursal.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new MiException("No existe la sucursal indicada"));
        sucursal.setEliminado(true);
        sucursal.setActiva(false);
        repositorioSucursal.save(sucursal);
    }

    private void validar(SucursalRequestDTO dto) throws MiException {
        if (dto == null || dto.direccion() == null) {
            throw new MiException("Indicá los datos de la sucursal y su dirección");
        }
        if (dto.nombre() == null || dto.nombre().isBlank()) {
            throw new MiException("El nombre de la sucursal es obligatorio");
        }
    }

    private void copiar(Sucursal sucursal, SucursalRequestDTO dto) {
        sucursal.setNombre(dto.nombre().trim());
        sucursal.setTelefono(textoOpcional(dto.telefono()));
        sucursal.setEmailContacto(textoOpcional(dto.emailContacto()));
        sucursal.setHorario(textoOpcional(dto.horario()));
        sucursal.setActiva(dto.activa() == null || dto.activa());
    }

    private SucursalResponseDTO convertir(Sucursal sucursal) {
        return new SucursalResponseDTO(sucursal.getId(), sucursal.getNombre(), sucursal.getTelefono(),
                sucursal.getEmailContacto(), sucursal.getHorario(),
                servicioDireccion.convertirAResponse(sucursal.getDireccion()), sucursal.isActiva(), sucursal.isEliminado());
    }

    private static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
