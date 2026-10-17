package com.incisog.Clinica.logic.factory;

import com.incisog.Clinica.models.Medico;
import org.springframework.stereotype.Component;

/** ConcreteCreator: fabrica Médicos. */
@Component
public class MedicoCreator extends PersonaCreator<Medico> {

    @Override
    protected Medico nuevaPersona() {
        return new Medico();
    }
}
