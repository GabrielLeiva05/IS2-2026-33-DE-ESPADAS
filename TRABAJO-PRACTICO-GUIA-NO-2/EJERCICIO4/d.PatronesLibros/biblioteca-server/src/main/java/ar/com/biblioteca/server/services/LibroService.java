package ar.com.biblioteca.server.services;

import ar.com.biblioteca.server.entities.Libro;
import ar.com.biblioteca.server.exceptions.BadRequestException;
import ar.com.biblioteca.server.repositories.LibroRepository;
import ar.com.biblioteca.server.strategy.CriterioBusquedaLibro;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Búsqueda de libros en todo el sistema (catálogo). Es el "Context" del patrón STRATEGY.
 * <p>
 * Los libros se siguen dando de alta / modificando a través de su Persona (composición, en
 * PersonaService); este service solo los consulta.
 */
@Service
public class LibroService {

    private final LibroRepository repository;

    /**
     * Clave = nombre del bean ("titulo", "genero", "autor"), valor = la estrategia.
     * Spring arma este Map solo, con todos los @Component que implementan CriterioBusquedaLibro.
     */
    private final Map<String, CriterioBusquedaLibro> criterios;

    public LibroService(LibroRepository repository, Map<String, CriterioBusquedaLibro> criterios) {
        this.repository = repository;
        this.criterios = criterios;
    }

    /**
     * Sin criterio: todos los libros. Con criterio: solo los que cumplen la estrategia elegida.
     */
    @Transactional(readOnly = true)
    public List<Libro> buscar(String criterio, String valor) {
        List<Libro> libros = repository.findAll(Sort.by("titulo"));
        if (criterio == null || criterio.isBlank()) {
            return libros;
        }
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("Debe indicar un valor de búsqueda");
        }

        CriterioBusquedaLibro estrategia = criterios.get(criterio.trim().toLowerCase());
        if (estrategia == null) {
            throw new BadRequestException("Criterio de búsqueda desconocido: " + criterio
                    + ". Valores válidos: " + String.join(", ", criterios.keySet()));
        }
        String buscado = valor.trim();
        return libros.stream().filter(libro -> estrategia.cumple(libro, buscado)).toList();
    }
}
