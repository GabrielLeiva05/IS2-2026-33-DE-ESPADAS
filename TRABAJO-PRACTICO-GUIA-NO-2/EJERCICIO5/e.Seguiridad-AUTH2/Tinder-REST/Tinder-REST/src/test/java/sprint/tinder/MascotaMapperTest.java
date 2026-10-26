package sprint.tinder;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sprint.tinder.dtos.MascotaDto;
import sprint.tinder.entities.Foto;
import sprint.tinder.entities.Mascota;
import sprint.tinder.entities.Usuario;
import sprint.tinder.enumerations.Sexo;
import sprint.tinder.enumerations.Tipo;
import sprint.tinder.mappers.MascotaMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MascotaMapperTest {
    private final MascotaMapper mapper = Mappers.getMapper(MascotaMapper.class);

    @Test
    void mapsPetFieldsAndPhotoUrlWithoutExposingImageContents() {
        Usuario usuario = new Usuario();
        usuario.setId("user-1");
        usuario.setNombre("Ana");
        usuario.setClave("secret-hash");

        Foto foto = new Foto();
        foto.setContenido(new byte[]{1, 2, 3});

        Mascota mascota = new Mascota();
        mascota.setId("pet-1");
        mascota.setNombre("Luna");
        mascota.setUsuario(usuario);
        mascota.setSexo(Sexo.HEMBRA);
        mascota.setTipo(Tipo.GATO);
        mascota.setFoto(foto);

        MascotaDto dto = mapper.toDto(mascota);

        assertEquals("pet-1", dto.getId());
        assertEquals("Luna", dto.getNombre());
        assertEquals("user-1", dto.getUsuarioId());
        assertEquals("Ana", dto.getUsuarioNombre());
        assertEquals(Sexo.HEMBRA, dto.getSexo());
        assertEquals(Tipo.GATO, dto.getTipo());
        assertEquals("/foto/mascota/pet-1", dto.getFotoUrl());
    }

    @Test
    void mapsNullPhotoToNullUrl() {
        Mascota mascota = new Mascota();
        mascota.setId("pet-2");

        MascotaDto dto = mapper.toDto(mascota);

        assertNull(dto.getFotoUrl());
    }
}
