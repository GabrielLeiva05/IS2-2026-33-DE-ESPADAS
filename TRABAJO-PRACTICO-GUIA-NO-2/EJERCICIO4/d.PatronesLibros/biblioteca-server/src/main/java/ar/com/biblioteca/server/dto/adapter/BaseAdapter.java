package ar.com.biblioteca.server.dto.adapter;

import java.util.List;

/**
 * Patrón estructural ADAPTER (mismo molde que en la Clínica).
 * <p>
 * La entidad (lo que maneja JPA y el service) y el DTO (lo que viaja en el JSON) tienen
 * "interfaces" distintas. El adapter traduce entre las dos, así ninguna de las dos tiene
 * que conocer a la otra:
 * <pre>
 *   JSON → DTO --toEntity()--> Entidad → service → Entidad --toDTO()--> DTO → JSON
 * </pre>
 * Se usa SOLO en los controllers: los services siguen trabajando con entidades.
 *
 * @param <E> entidad
 * @param <D> DTO
 */
public abstract class BaseAdapter<E, D> {

    public abstract D toDTO(E entidad);

    public abstract E toEntity(D dto);

    public List<D> toDTOList(List<E> entidades) {
        return entidades.stream().map(this::toDTO).toList();
    }
}
