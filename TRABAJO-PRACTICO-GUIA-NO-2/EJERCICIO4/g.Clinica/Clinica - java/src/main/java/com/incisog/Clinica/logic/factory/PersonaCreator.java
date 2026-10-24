package com.incisog.Clinica.logic.factory;

import com.incisog.Clinica.models.Persona;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Patrón creacional FACTORY METHOD — el "Creator".
 *
 * crear(...) es el algoritmo COMÚN para fabricar cualquier persona: normaliza los datos
 * y los asigna. El único paso que varía es QUÉ clase concreta se instancia, y eso lo decide
 * cada subclase implementando el factory method nuevaPersona().
 *
 *   PersonaCreator (Creator)          → crear() + nuevaPersona() abstracto
 *     ├─ PacienteCreator (ConcreteCreator) → new Paciente()
 *     └─ MedicoCreator   (ConcreteCreator) → new Medico()
 *
 * Por qué normalizar acá: sin esto, "30.111.222" y "30111222" serían documentos distintos
 * y la validación de duplicados no los detectaría; "  juan  pérez" quedaría mal escrito.
 * Al estar en el Creator, la regla vale igual para TODAS las personas del sistema.
 *
 * Nota: crear() es a su vez un pequeño Template Method (GoF: "los factory methods suelen
 * ser llamados desde template methods").
 */
public abstract class PersonaCreator<T extends Persona> {

    /** Algoritmo común de creación (no lo redefinen las subclases). */
    public T crear(String nombre, String apellido, String documento) {
        T persona = nuevaPersona();                       // ← FACTORY METHOD: la subclase decide la clase
        persona.setNombre(normalizarNombre(nombre));
        persona.setApellido(normalizarNombre(apellido));
        persona.setDocumento(normalizarDocumento(documento));
        persona.setEliminado(false);
        return persona;
    }

    /** Factory method: cada creator concreto devuelve su producto concreto. */
    protected abstract T nuevaPersona();

    // ---------- reglas de normalización comunes ----------

    /** "  juan   PÉREZ " → "Juan Pérez". Si viene null, queda null (lo rechaza validar() del service). */
    protected String normalizarNombre(String texto) {
        if (texto == null) return null;
        String limpio = texto.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) return limpio;
        return Arrays.stream(limpio.split(" "))
                .map(p -> p.substring(0, 1).toUpperCase(Locale.ROOT) + p.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }

    /** "30.111.222" o "30 111-222" → "30111222". Quita puntos, espacios y guiones. */
    protected String normalizarDocumento(String documento) {
        if (documento == null) return null;
        return documento.replaceAll("[.\\s-]", "").toUpperCase(Locale.ROOT);
    }
}
