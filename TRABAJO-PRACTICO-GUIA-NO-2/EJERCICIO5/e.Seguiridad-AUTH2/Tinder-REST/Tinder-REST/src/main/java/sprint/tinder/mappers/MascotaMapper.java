package sprint.tinder.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sprint.tinder.dtos.MascotaDto;
import sprint.tinder.entities.Mascota;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MascotaMapper {
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioNombre", source = "usuario.nombre")
    @Mapping(target = "fotoUrl", expression = "java(mascota.getFoto() == null ? null : \"/foto/mascota/\" + mascota.getId())")
    MascotaDto toDto(Mascota mascota);

    List<MascotaDto> toDtos(Collection<Mascota> mascotas);
}
