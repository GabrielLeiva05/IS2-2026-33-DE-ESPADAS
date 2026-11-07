package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ReordenarVisitasRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.VisitaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.model.Visita;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPuntoVenta;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioVisita;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Planificación diaria de visitas del preventista (Fase 7). La sugerencia inicial es por cercanía
 * en línea recta y el orden siempre se puede corregir a mano.
 */
@Service
public class ServicioVisita {

    private final RepositorioVisita repositorioVisita;
    private final RepositorioPuntoVenta repositorioPuntoVenta;
    private final ServicioUsuario servicioUsuario;
    private final ServicioEmpleado servicioEmpleado;

    public ServicioVisita(RepositorioVisita repositorioVisita, RepositorioPuntoVenta repositorioPuntoVenta,
                          ServicioUsuario servicioUsuario, ServicioEmpleado servicioEmpleado) {
        this.repositorioVisita = repositorioVisita;
        this.repositorioPuntoVenta = repositorioPuntoVenta;
        this.servicioUsuario = servicioUsuario;
        this.servicioEmpleado = servicioEmpleado;
    }

    @Transactional(readOnly = true)
    public VisitaResponseDTO.PlanVisitasDTO planDelDia(String correoPreventista, LocalDate fecha) throws MiException {
        Preventista preventista = preventistaDeCuenta(correoPreventista);
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        return new VisitaResponseDTO.PlanVisitasDTO(dia.toString(), convertir(
                repositorioVisita.findByPreventista_IdAndFechaAndEliminadoFalseOrderByOrdenAsc(preventista.getId(), dia)));
    }

    /** Arma (o completa) la planificación del día con la cartera del preventista. */
    @Transactional(rollbackFor = Exception.class)
    public VisitaResponseDTO.PlanVisitasDTO generar(String correoPreventista, LocalDate fecha) throws MiException {
        Preventista preventista = preventistaDeCuenta(correoPreventista);
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        List<Visita> existentes = new ArrayList<>(
                repositorioVisita.findByPreventista_IdAndFechaAndEliminadoFalse(preventista.getId(), dia));
        List<String> yaIncluidos = existentes.stream().map(visita -> visita.getPuntoVenta().getId()).toList();
        List<PuntoVenta> pendientes = repositorioPuntoVenta
                .findByPreventista_IdAndEliminadoFalseOrderByNombreComercialAsc(preventista.getId()).stream()
                .filter(punto -> !yaIncluidos.contains(punto.getId()))
                .toList();
        if (pendientes.isEmpty() && existentes.isEmpty()) {
            throw new MiException("Tu cartera todavía no tiene comercios para visitar");
        }
        int orden = existentes.stream().mapToInt(Visita::getOrden).max().orElse(0);
        for (PuntoVenta punto : ordenarPorCercania(pendientes)) {
            Visita visita = new Visita();
            visita.setPreventista(preventista);
            visita.setFecha(dia);
            visita.setPuntoVenta(punto);
            visita.setOrden(++orden);
            existentes.add(repositorioVisita.save(visita));
        }
        return new VisitaResponseDTO.PlanVisitasDTO(dia.toString(), convertir(
                repositorioVisita.findByPreventista_IdAndFechaAndEliminadoFalseOrderByOrdenAsc(preventista.getId(), dia)));
    }

    @Transactional(rollbackFor = Exception.class)
    public VisitaResponseDTO.PlanVisitasDTO reordenar(String correoPreventista, LocalDate fecha,
                                                      ReordenarVisitasRequestDTO dto) throws MiException {
        Preventista preventista = preventistaDeCuenta(correoPreventista);
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        List<Visita> visitas = repositorioVisita.findByPreventista_IdAndFechaAndEliminadoFalse(preventista.getId(), dia);
        if (dto == null || dto.idsVisitas() == null || dto.idsVisitas().size() != visitas.size()) {
            throw new MiException("El nuevo orden debe incluir todas las visitas del día");
        }
        int orden = 1;
        for (String idVisita : dto.idsVisitas()) {
            Visita visita = visitas.stream().filter(v -> Objects.equals(v.getId(), idVisita)).findFirst()
                    .orElseThrow(() -> new MiException("La visita no pertenece a tu planificación del día"));
            visita.setOrden(orden++);
            repositorioVisita.save(visita);
        }
        return planDelDia(correoPreventista, dia);
    }

    @Transactional(rollbackFor = Exception.class)
    public void quitar(String correoPreventista, String idVisita) throws MiException {
        Preventista preventista = preventistaDeCuenta(correoPreventista);
        Visita visita = repositorioVisita.findByIdAndEliminadoFalse(idVisita)
                .orElseThrow(() -> new MiException("No existe la visita indicada"));
        if (!Objects.equals(visita.getPreventista().getId(), preventista.getId())) {
            throw new MiException("La visita no pertenece a tu planificación");
        }
        visita.setEliminado(true);
        repositorioVisita.save(visita);
    }

    // ------------------------------------------------------------------ auxiliares

    private Preventista preventistaDeCuenta(String correo) throws MiException {
        Usuario usuario = servicioUsuario.buscarActivoPorNombreUsuario(correo)
                .orElseThrow(() -> new MiException("No se encontró la cuenta autenticada"));
        if (usuario.getRol() != RolUsuario.PREVENTISTA && usuario.getRol() != RolUsuario.JEFE) {
            throw new MiException("La cuenta autenticada no tiene perfil de preventista");
        }
        return servicioEmpleado.buscarPorUsuario(usuario.getId())
                .filter(Preventista.class::isInstance)
                .map(Preventista.class::cast)
                .orElseThrow(() -> new MiException("No se encontró el preventista asociado a la cuenta"));
    }

    /** Vecino más cercano en línea recta; los comercios sin coordenadas van al final por nombre. */
    private static List<PuntoVenta> ordenarPorCercania(List<PuntoVenta> puntos) {
        List<PuntoVenta> conCoordenadas = new ArrayList<>(puntos.stream()
                .filter(punto -> coordenadas(punto) != null).toList());
        List<PuntoVenta> sinCoordenadas = puntos.stream()
                .filter(punto -> coordenadas(punto) == null)
                .sorted(Comparator.comparing(PuntoVenta::getNombreComercial, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<PuntoVenta> ordenados = new ArrayList<>();
        if (!conCoordenadas.isEmpty()) {
            conCoordenadas.sort(Comparator.comparingDouble(punto -> coordenadas(punto)[0]
                    + coordenadas(punto)[1]));
            PuntoVenta actual = conCoordenadas.remove(0);
            ordenados.add(actual);
            while (!conCoordenadas.isEmpty()) {
                double[] origen = coordenadas(actual);
                PuntoVenta siguiente = null;
                double menor = Double.MAX_VALUE;
                for (PuntoVenta candidato : conCoordenadas) {
                    double[] destino = coordenadas(candidato);
                    double distancia = Math.pow(origen[0] - destino[0], 2) + Math.pow(origen[1] - destino[1], 2);
                    if (distancia < menor) {
                        menor = distancia;
                        siguiente = candidato;
                    }
                }
                ordenados.add(siguiente);
                conCoordenadas.remove(siguiente);
                actual = siguiente;
            }
        }
        ordenados.addAll(sinCoordenadas);
        return ordenados;
    }

    private static double[] coordenadas(PuntoVenta punto) {
        Direccion direccion = punto.getDireccion();
        if (direccion == null || direccion.getLatitud() == null || direccion.getLongitud() == null) {
            return null;
        }
        return new double[]{direccion.getLatitud(), direccion.getLongitud()};
    }

    private List<VisitaResponseDTO> convertir(List<Visita> visitas) {
        return visitas.stream().map(visita -> {
            PuntoVenta punto = visita.getPuntoVenta();
            Direccion direccion = punto.getDireccion();
            return new VisitaResponseDTO(visita.getId(), punto.getId(), punto.getNombreComercial(),
                    direccion.getCalle(), direccion.getNumeracion(),
                    direccion.getLocalidad() == null ? null : direccion.getLocalidad().getNombre(),
                    direccion.getLatitud(), direccion.getLongitud(), visita.getOrden(), visita.getObservacion());
        }).toList();
    }
}
