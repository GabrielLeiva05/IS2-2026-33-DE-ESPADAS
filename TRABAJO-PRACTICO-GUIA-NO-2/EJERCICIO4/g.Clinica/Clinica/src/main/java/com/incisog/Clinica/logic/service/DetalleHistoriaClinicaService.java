package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.DetalleHistoriaClinicaRepository;
import com.incisog.Clinica.logic.strategy.CriterioBusquedaDetalle;
import com.incisog.Clinica.models.DetalleHistoriaClinica;
import com.incisog.Clinica.models.HistoriaClinica;
import com.incisog.Clinica.models.Medico;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class DetalleHistoriaClinicaService {
    private final DetalleHistoriaClinicaRepository repository;
    private final HistoriaClinicaService historiaService;
    private final MedicoService medicoService;

    //El contexto no es una interface en este caso, si no la misma clase DetalleHistoriaClinicaService
    private final Map<String, CriterioBusquedaDetalle> criterios;


    public DetalleHistoriaClinicaService(DetalleHistoriaClinicaRepository repository,
                                         HistoriaClinicaService historiaService,
                                         MedicoService medicoService,
                                         Map<String, CriterioBusquedaDetalle> criterios) {
        this.repository = repository;
        this.historiaService = historiaService;
        this.medicoService = medicoService;
        this.criterios = criterios;
    }

    /** Detalles activos de la historia, del más reciente al más antiguo. */
    @Transactional(readOnly = true, rollbackFor = ErrorServiceException.class)
    public List<DetalleHistoriaClinica> listar(String historiaId) throws ErrorServiceException {
        HistoriaClinica historia = historiaService.buscarPorId(historiaId);
        return historia.getDetalles().stream()
                .filter(detalle -> !detalle.isEliminado())
                .sorted(Comparator.comparing(DetalleHistoriaClinica::getFechaHistoria).reversed())
                .toList();
    }

    /**
     * Agrega una atención a la historia.
     * JSON esperado: {"fechaHistoria": "2026-10-13", "detalleHistoria": "...", "medico": {"id": "..."}}
     */
    @Transactional(rollbackFor = ErrorServiceException.class)
    public DetalleHistoriaClinica agregar(String historiaId, DetalleHistoriaClinica detalle) throws ErrorServiceException {
        HistoriaClinica historia = historiaService.buscarPorId(historiaId);
        validar(detalle);

        detalle.setId(null);
        detalle.setEliminado(false);
        detalle.setMedico(buscarMedico(detalle));

        // 1) Se guarda el detalle (INSERT, se genera su id).
        // 2) Se agrega a la lista de la historia: al terminar la transacción, JPA
        //    completa la FK historia_clinica_id del detalle.
        DetalleHistoriaClinica guardado = repository.save(detalle);
        historia.getDetalles().add(guardado);
        return guardado;
    }

    @Transactional(rollbackFor = ErrorServiceException.class)
    public DetalleHistoriaClinica modificar(String historiaId, String detalleId, DetalleHistoriaClinica datos) throws ErrorServiceException {
        validar(datos);
        DetalleHistoriaClinica detalle = buscarEnHistoria(historiaId, detalleId);

        detalle.setFechaHistoria(datos.getFechaHistoria());
        detalle.setDetalleHistoria(datos.getDetalleHistoria());
        detalle.setMedico(buscarMedico(datos));
        return repository.save(detalle);
    }

    @Transactional(rollbackFor = ErrorServiceException.class)
    public void eliminar(String historiaId, String detalleId) throws ErrorServiceException {
        DetalleHistoriaClinica detalle = buscarEnHistoria(historiaId, detalleId);
        detalle.setEliminado(true);
        repository.save(detalle);
    }

    /** Busca el detalle DENTRO de la historia indicada: un detalle de otra historia no se encuentra. */
    private DetalleHistoriaClinica buscarEnHistoria(String historiaId, String detalleId) throws ErrorServiceException {
        HistoriaClinica historia = historiaService.buscarPorId(historiaId);
        return historia.getDetalles().stream()
                .filter(detalle -> detalle.getId().equals(detalleId) && !detalle.isEliminado())
                .findFirst()
                .orElseThrow(() -> new ErrorServiceException("Detalle no encontrado en esta historia clínica."));
    }

    private Medico buscarMedico(DetalleHistoriaClinica detalle) throws ErrorServiceException {
        if (detalle.getMedico() == null || detalle.getMedico().getId() == null) {
            throw new ErrorServiceException("Debe indicar el médico");
        }
        return medicoService.buscarPorId(detalle.getMedico().getId());
    }

    private void validar(DetalleHistoriaClinica detalle) throws ErrorServiceException {
        if (detalle == null) {
            throw new ErrorServiceException("Debe indicar el detalle");
        }
        if (detalle.getFechaHistoria() == null) {
            throw new ErrorServiceException("Debe indicar la fecha");
        }
        if (detalle.getFechaHistoria().isAfter(LocalDate.now())) {
            throw new ErrorServiceException("La fecha no puede ser futura");
        }
        if (detalle.getDetalleHistoria() == null || detalle.getDetalleHistoria().trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el detalle de la atención");
        }
    }

    public List<DetalleHistoriaClinica> buscar(String historiaId, String criterio, String valor) throws ErrorServiceException {
        //Se repite mucho la palabra Criterio.. JAJA
        //criterios es la coleccion clave/valor que tenemos
        //criterio es la clave
        //criterioObj es el valor que recibimos con la clave correspondiente (La estrategia que vamos a usar)

        //algunas validaciones
        if (valor == null || valor.isBlank()) {
            throw new ErrorServiceException("Debe indicar un valor de búsqueda");
        }

        CriterioBusquedaDetalle criterioObj = criterios.get(criterio);   // la estrategia elegida por su clave
        if (criterioObj == null) {
            throw new ErrorServiceException("Criterio desconocido: " + criterio);
        }
        List<DetalleHistoriaClinica> listaBase = listar(historiaId);
        try {
            return listaBase.stream().filter(det -> criterioObj.cumple(det, valor)).toList();
        } catch (IllegalArgumentException e) {
            throw new ErrorServiceException(e.getMessage());
        }

    }
}
