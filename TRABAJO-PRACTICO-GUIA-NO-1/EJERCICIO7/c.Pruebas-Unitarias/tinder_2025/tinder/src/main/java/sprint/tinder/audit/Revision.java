package sprint.tinder.audit;

import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import sprint.tinder.config.CustomRevisionListener;

@Entity
@Table(name = "revision_info")
@RevisionEntity(CustomRevisionListener.class)
public class Revision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // numero de revision (equivalente al REV de REVINFO)
    @RevisionNumber
    private int id;

    // fecha/hora (equivalente al REVTSTMP de REVINFO)
    @RevisionTimestamp
    private long fecha;

    // ---- A PARTIR DE ACA, campo custom que Envers NO llena solo ----
    // Lo completa CustomRevisionListener con el id del usuario logueado
    // (leído de UsuarioActualHolder), no Envers directamente.
    private String usuario;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
