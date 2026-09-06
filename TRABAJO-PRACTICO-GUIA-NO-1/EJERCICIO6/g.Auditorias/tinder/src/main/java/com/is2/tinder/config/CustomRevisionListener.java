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
    }
}