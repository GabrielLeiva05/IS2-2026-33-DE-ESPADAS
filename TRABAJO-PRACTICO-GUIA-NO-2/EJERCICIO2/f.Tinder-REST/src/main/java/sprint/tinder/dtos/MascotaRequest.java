package sprint.tinder.dtos;

import lombok.Getter;
import lombok.Setter;
import sprint.tinder.enumerations.Sexo;
import sprint.tinder.enumerations.Tipo;

@Getter
@Setter
public class MascotaRequest {
    private String nombre;
    private Sexo sexo;
    private Tipo tipo;
}
