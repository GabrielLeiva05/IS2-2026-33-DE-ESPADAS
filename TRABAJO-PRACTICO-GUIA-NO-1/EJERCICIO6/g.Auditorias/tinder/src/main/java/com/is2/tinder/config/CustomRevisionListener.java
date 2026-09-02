package com.is2.tinder.config;

import com.is2.tinder.audit.Revision;
import com.is2.tinder.audit.UsuarioActualHolder;
import org.hibernate.envers.RevisionListener;

// Envers llama a este metodo UNA VEZ por cada Revision que va a crear,
// justo antes de guardarla (todavia no esta persistida). Es el unico
// lugar donde podes completar los campos custom de Revision, porque
// esta clase NO es un bean de Spring (Envers la instancia el sola,
// via reflexion), asi que aca NO se puede usar @Autowired.
public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        final Revision revision = (Revision) revisionEntity;

        // Si hubiera una entidad Usuario (con login), aca iria algo asi:
        //
        revision.setUsuario(UsuarioActualHolder.get());
        //
        // usando un ThreadLocal (UsuarioActualHolder) que un interceptor
        // de Spring completa al principio de cada request con el usuario
        // logueado (SecurityContextHolder). No se puede inyectar el
        // usuario actual directamente aca porque, como dijimos arriba,
        // esta clase no vive dentro del contenedor de Spring.
        //
        // Como este proyecto no tiene usuarios todavia, no seteamos nada:
        // la Revision se guarda solo con id y fecha (autocompletados por
        // Envers gracias a @RevisionNumber y @RevisionTimestamp).
    }
}