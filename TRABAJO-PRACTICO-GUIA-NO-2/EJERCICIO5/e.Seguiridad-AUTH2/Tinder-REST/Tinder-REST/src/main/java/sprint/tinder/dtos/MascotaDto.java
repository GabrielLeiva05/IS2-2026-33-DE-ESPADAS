package sprint.tinder.dtos;

import lombok.Getter;
import lombok.Setter;
import sprint.tinder.enumerations.Sexo;
import sprint.tinder.enumerations.Tipo;

import java.util.Date;

@Getter
@Setter
public class MascotaDto {
    private String id;
    private String nombre;
    private String usuarioId;
    private String usuarioNombre;
    private Sexo sexo;
    private Tipo tipo;
    private Date alta;
    private Date baja;
    private String fotoUrl;
}
