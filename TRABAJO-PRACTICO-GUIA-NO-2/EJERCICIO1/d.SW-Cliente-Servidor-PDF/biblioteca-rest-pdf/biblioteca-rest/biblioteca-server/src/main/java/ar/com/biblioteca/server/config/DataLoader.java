package ar.com.biblioteca.server.config;

import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.entities.Domicilio;
import ar.com.biblioteca.server.entities.Libro;
import ar.com.biblioteca.server.entities.Localidad;
import ar.com.biblioteca.server.entities.Persona;
import ar.com.biblioteca.server.repositories.AutorRepository;
import ar.com.biblioteca.server.repositories.LocalidadRepository;
import ar.com.biblioteca.server.repositories.PersonaRepository;
import ar.com.biblioteca.server.services.PersonaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Carga datos de ejemplo al iniciar la aplicación, solo si la base de datos está vacía.
 * Sirve para poder probar el cliente inmediatamente.
 */
@Configuration
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner cargarDatosDeEjemplo(LocalidadRepository localidadRepository,
                                           AutorRepository autorRepository,
                                           PersonaRepository personaRepository,
                                           PersonaService personaService) {
        return args -> {
            if (localidadRepository.count() > 0 || autorRepository.count() > 0 || personaRepository.count() > 0) {
                log.info("La base de datos ya contiene información: no se cargan datos de ejemplo.");
                return;
            }

            // Localidades
            Localidad capital = localidadRepository.save(new Localidad("Mendoza (Capital)"));
            Localidad godoyCruz = localidadRepository.save(new Localidad("Godoy Cruz"));
            localidadRepository.save(new Localidad("Guaymallén"));
            localidadRepository.save(new Localidad("Las Heras"));
            localidadRepository.save(new Localidad("Maipú"));
            localidadRepository.save(new Localidad("Luján de Cuyo"));

            // Autores
            Autor garciaMarquez = autorRepository.save(new Autor("Gabriel", "García Márquez",
                    "Escritor colombiano, referente del realismo mágico. Premio Nobel de Literatura 1982."));
            Autor borges = autorRepository.save(new Autor("Jorge Luis", "Borges",
                    "Escritor argentino de cuentos, ensayos y poemas, célebre por sus relatos fantásticos."));
            Autor cortazar = autorRepository.save(new Autor("Julio", "Cortázar",
                    "Escritor argentino, maestro del cuento breve y autor de novelas experimentales."));
            Autor allende = autorRepository.save(new Autor("Isabel", "Allende",
                    "Escritora chilena, una de las autoras de habla hispana más leídas del mundo."));

            // Persona 1: domicilio en Capital, dos libros
            Persona ana = new Persona("Ana", "Martínez", 30123456,
                    new Domicilio("Av. San Martín", 1234, capital));
            ana.getLibros().add(libro("Cien años de soledad", 1967, "Novela", 471, garciaMarquez));
            ana.getLibros().add(libro("Ficciones", 1944, "Cuentos", 174, borges));
            personaService.create(ana);

            // Persona 2: domicilio en Godoy Cruz, un libro con dos autores
            Persona luis = new Persona("Luis", "Fernández", 28456789,
                    new Domicilio("Calle Belgrano", 567, godoyCruz));
            luis.getLibros().add(libro("Rayuela", 1963, "Novela", 635, cortazar));
            luis.getLibros().add(libro("La casa de los espíritus", 1982, "Novela", 433, allende, garciaMarquez));
            personaService.create(luis);

            log.info("Datos de ejemplo cargados: 6 localidades, 4 autores y 2 personas con sus libros.");
        };
    }

    private Libro libro(String titulo, int fecha, String genero, int paginas, Autor... autores) {
        Libro libro = new Libro(titulo, fecha, genero, paginas);
        List<Autor> lista = new ArrayList<>(List.of(autores));
        libro.setAutores(lista);
        return libro;
    }
}
