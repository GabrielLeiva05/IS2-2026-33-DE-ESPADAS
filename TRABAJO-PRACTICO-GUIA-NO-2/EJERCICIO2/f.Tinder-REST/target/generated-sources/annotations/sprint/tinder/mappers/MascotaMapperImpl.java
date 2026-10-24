package sprint.tinder.mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sprint.tinder.dtos.MascotaDto;
import sprint.tinder.entities.Mascota;
import sprint.tinder.entities.Usuario;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T17:36:18-0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260826-1225, environment: Java 21.0.12.1 (Eclipse Adoptium)"
)
@Component
public class MascotaMapperImpl implements MascotaMapper {

    @Override
    public MascotaDto toDto(Mascota mascota) {
        if ( mascota == null ) {
            return null;
        }

        MascotaDto mascotaDto = new MascotaDto();

        mascotaDto.setUsuarioId( mascotaUsuarioId( mascota ) );
        mascotaDto.setUsuarioNombre( mascotaUsuarioNombre( mascota ) );
        mascotaDto.setAlta( mascota.getAlta() );
        mascotaDto.setBaja( mascota.getBaja() );
        mascotaDto.setId( mascota.getId() );
        mascotaDto.setNombre( mascota.getNombre() );
        mascotaDto.setSexo( mascota.getSexo() );
        mascotaDto.setTipo( mascota.getTipo() );

        mascotaDto.setFotoUrl( mascota.getFoto() == null ? null : "/foto/mascota/" + mascota.getId() );

        return mascotaDto;
    }

    @Override
    public List<MascotaDto> toDtos(Collection<Mascota> mascotas) {
        if ( mascotas == null ) {
            return null;
        }

        List<MascotaDto> list = new ArrayList<MascotaDto>( mascotas.size() );
        for ( Mascota mascota : mascotas ) {
            list.add( toDto( mascota ) );
        }

        return list;
    }

    private String mascotaUsuarioId(Mascota mascota) {
        Usuario usuario = mascota.getUsuario();
        if ( usuario == null ) {
            return null;
        }
        return usuario.getId();
    }

    private String mascotaUsuarioNombre(Mascota mascota) {
        Usuario usuario = mascota.getUsuario();
        if ( usuario == null ) {
            return null;
        }
        return usuario.getNombre();
    }
}
