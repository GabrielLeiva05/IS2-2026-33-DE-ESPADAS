package sprint.tinder.entities;

import jakarta.persistence.*;

import java.util.Date;

import org.hibernate.annotations.UuidGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sprint.tinder.enumerations.Rol;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="usuario")
@Entity
@Audited
public class Usuario {
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;
    private String nombre;
    private String apellido;
    private String mail;
    private String clave;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @ManyToOne
    @NotAudited
    private Zona zona;

    @Temporal(TemporalType.TIMESTAMP)
    private Date alta;
    @Temporal (TemporalType.TIMESTAMP)
    private Date baja;

    @OneToOne
    @NotAudited
    private Foto foto;

    private boolean eliminado;
}

