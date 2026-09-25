package sprint.tinder.config;

import org.hibernate.envers.RevisionListener;
import sprint.tinder.audit.Revision;
import sprint.tinder.audit.UsuarioActualHolder;

// Envers llama a este metodo UNA VEZ por cada Revision que va a crear,
// justo antes de guardarla (todavia no esta persistida). Es el unico
// lugar donde podes completar los campos custom de Revision, porque
// esta clase NO es un bean de Spring (Envers la instancia el sola,
// via reflexion), asi que aca NO se puede usar @Autowired.
public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        final Revision revision = (Revision) revisionEntity;

        // Usamos un ThreadLocal (UsuarioActualHolder) que el propio
        // controller completa al principio de la operación con el id
        // del usuario logueado (usuariosession). No se puede inyectar
        // el usuario actual directamente acá porque, como dijimos
        // arriba, esta clase no vive dentro del contenedor de Spring.
        revision.setUsuario(UsuarioActualHolder.get());
    }
}
