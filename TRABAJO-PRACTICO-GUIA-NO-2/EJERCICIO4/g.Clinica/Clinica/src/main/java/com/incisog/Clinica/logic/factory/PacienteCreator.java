package com.incisog.Clinica.logic.factory;

import com.incisog.Clinica.models.Paciente;
import org.springframework.stereotype.Component;

/** ConcreteCreator: fabrica Pacientes. */
@Component
public class PacienteCreator extends PersonaCreator<Paciente> {

    @Override
    protected Paciente nuevaPersona() {
        return new Paciente();
    }
}
