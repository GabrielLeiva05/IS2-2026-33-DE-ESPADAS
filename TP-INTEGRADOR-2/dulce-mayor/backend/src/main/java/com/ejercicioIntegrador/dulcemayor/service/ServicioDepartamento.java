package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.DepartamentoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DepartamentoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Departamento;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioDepartamento;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Reutiliza la validación de DepartamentoServicio del Integrador 1, ahora dentro del Template Method. */
@Service
public class ServicioDepartamento extends BaseService<Departamento, DepartamentoRequestDTO, DepartamentoResponseDTO> {

    private final RepositorioDepartamento repositorioDepartamento;
    private final ServicioProvincia servicioProvincia;

    public ServicioDepartamento(RepositorioDepartamento repositorioDepartamento, ServicioProvincia servicioProvincia) {
        super(repositorioDepartamento);
        this.repositorioDepartamento = repositorioDepartamento;
        this.servicioProvincia = servicioProvincia;
    }

    @Override
    protected Departamento crearEntidad(DepartamentoRequestDTO dto) throws MiException {
        Departamento departamento = new Departamento();
        actualizarEntidad(departamento, dto);
        return departamento;
    }

    @Override
    protected void actualizarEntidad(Departamento departamento, DepartamentoRequestDTO dto) throws MiException {
        departamento.setNombre(normalizarTexto(dto.nombre()));
        departamento.setProvincia(obtenerReferencia(servicioProvincia, dto.idProvincia(), "la provincia"));
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Departamento departamento) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(departamento.getNombre())) {
            throw new MiException("El nombre del departamento no puede estar vacío");
        }
        if (departamento.getProvincia() == null || departamento.getProvincia().isEliminado()) {
            throw new MiException("Debe asociar una provincia activa al departamento");
        }
        Optional<Departamento> existente = repositorioDepartamento.findFirstByProvincia_IdAndNombreIgnoreCaseAndEliminadoFalse(
                departamento.getProvincia().getId(), departamento.getNombre());
        if (existente.isPresent() && !Objects.equals(existente.get().getId(), departamento.getId())) {
            throw new MiException("La provincia ya tiene un departamento llamado " + departamento.getNombre());
        }
    }

    @Override
    public DepartamentoResponseDTO convertirAResponse(Departamento departamento) {
        return new DepartamentoResponseDTO(departamento.getId(), departamento.getNombre(),
                departamento.getProvincia().getId(), departamento.getProvincia().getNombre(), departamento.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "el departamento";
    }

    @Transactional(readOnly = true)
    public DepartamentoResponseDTO buscarDepartamentoPorNombre(String nombre) throws MiException {
        if (estaVacio(nombre)) {
            throw new MiException("Debe indicar el nombre a buscar");
        }
        return repositorioDepartamento.findFirstByNombreIgnoreCaseAndEliminadoFalse(nombre.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el departamento " + nombre));
    }

    @Transactional(readOnly = true)
    public List<DepartamentoResponseDTO> listarDepartamento(String idProvincia) throws MiException {
        servicioProvincia.obtenerEntidad(idProvincia);
        return convertirAResponse(repositorioDepartamento.findByProvincia_IdOrderByNombreAsc(idProvincia));
    }

    @Transactional(readOnly = true)
    public List<DepartamentoResponseDTO> listarDepartamentoActivo(String idProvincia) throws MiException {
        servicioProvincia.obtenerEntidad(idProvincia);
        return convertirAResponse(repositorioDepartamento.findByProvincia_IdAndEliminadoFalseOrderByNombreAsc(idProvincia));
    }
}
