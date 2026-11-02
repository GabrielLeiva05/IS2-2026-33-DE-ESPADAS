package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.DireccionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DireccionResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Departamento;
import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.Localidad;
import com.ejercicioIntegrador.dulcemayor.model.Pais;
import com.ejercicioIntegrador.dulcemayor.model.Provincia;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioDireccion;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reutiliza las validaciones de DireccionServicio del Integrador 1 (calle, numeración y localidad obligatorias).
 * Latitud y longitud son opcionales: si se informa una, se exige la otra y que estén en rango.
 */
@Service
public class ServicioDireccion extends BaseService<Direccion, DireccionRequestDTO, DireccionResponseDTO> {

    private final RepositorioDireccion repositorioDireccion;
    private final ServicioLocalidad servicioLocalidad;

    public ServicioDireccion(RepositorioDireccion repositorioDireccion, ServicioLocalidad servicioLocalidad) {
        super(repositorioDireccion);
        this.repositorioDireccion = repositorioDireccion;
        this.servicioLocalidad = servicioLocalidad;
    }

    @Override
    protected Direccion crearEntidad(DireccionRequestDTO dto) throws MiException {
        Direccion direccion = new Direccion();
        actualizarEntidad(direccion, dto);
        return direccion;
    }

    @Override
    protected void actualizarEntidad(Direccion direccion, DireccionRequestDTO dto) throws MiException {
        direccion.setCalle(normalizarTexto(dto.calle()));
        direccion.setNumeracion(normalizarTexto(dto.numeracion()));
        direccion.setBarrio(vacioANulo(dto.barrio()));
        direccion.setManzanaPiso(vacioANulo(dto.manzanaPiso()));
        direccion.setCasaDepartamento(vacioANulo(dto.casaDepartamento()));
        direccion.setReferencia(vacioANulo(dto.referencia()));
        direccion.setLatitud(dto.latitud());
        direccion.setLongitud(dto.longitud());
        direccion.setLocalidad(obtenerReferencia(servicioLocalidad, dto.idLocalidad(), "la localidad"));
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Direccion direccion) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(direccion.getCalle())) {
            throw new MiException("Debe indicar la calle");
        }
        if (estaVacio(direccion.getNumeracion())) {
            throw new MiException("Debe indicar la numeración (o S/N)");
        }
        if (direccion.getLocalidad() == null || direccion.getLocalidad().isEliminado()) {
            throw new MiException("Debe seleccionar una localidad activa");
        }
        Double latitud = direccion.getLatitud();
        Double longitud = direccion.getLongitud();
        if ((latitud == null) != (longitud == null)) {
            throw new MiException("Debe indicar latitud y longitud juntas");
        }
        if (latitud != null && (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180)) {
            throw new MiException("Las coordenadas están fuera de rango");
        }
    }

    @Override
    public DireccionResponseDTO convertirAResponse(Direccion direccion) {
        Localidad localidad = direccion.getLocalidad();
        Departamento departamento = localidad.getDepartamento();
        Provincia provincia = departamento.getProvincia();
        Pais pais = provincia.getPais();
        return new DireccionResponseDTO(direccion.getId(), direccion.getCalle(), direccion.getNumeracion(),
                direccion.getBarrio(), direccion.getManzanaPiso(), direccion.getCasaDepartamento(),
                direccion.getReferencia(), direccion.getLatitud(), direccion.getLongitud(),
                localidad.getId(), localidad.getNombre(), localidad.getCodigoPostal(),
                departamento.getId(), departamento.getNombre(), provincia.getId(), provincia.getNombre(),
                pais.getId(), pais.getNombre(), direccion.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "la dirección";
    }

    /** crearDireccion(...): Direccion del diagrama. Usa el algoritmo de alta y devuelve la entidad. */
    @Transactional(rollbackFor = Exception.class)
    public Direccion crearDireccion(DireccionRequestDTO dto) throws MiException {
        return altaEntidad(dto);
    }

    @Transactional(readOnly = true)
    public DireccionResponseDTO buscarDireccionPorCalleNumeracion(String calle, String numeracion) throws MiException {
        if (estaVacio(calle) || estaVacio(numeracion)) {
            throw new MiException("Debe indicar calle y numeración");
        }
        return repositorioDireccion
                .findFirstByCalleIgnoreCaseAndNumeracionIgnoreCaseAndEliminadoFalse(calle.trim(), numeracion.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la dirección " + calle + " " + numeracion));
    }

    private static String vacioANulo(String valor) {
        return estaVacio(valor) ? null : valor.trim();
    }
}
