package ar.com.biblioteca.server.config;

import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.entities.Domicilio;
import ar.com.biblioteca.server.entities.Localidad;
import ar.com.biblioteca.server.entities.Persona;
import ar.com.biblioteca.server.factory.LibroDigitalCreator;
import ar.com.biblioteca.server.factory.LibroFisicoCreator;
import ar.com.biblioteca.server.repositories.AutorRepository;
import ar.com.biblioteca.server.repositories.LocalidadRepository;
import ar.com.biblioteca.server.repositories.PersonaRepository;
import ar.com.biblioteca.server.services.PersonaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Carga datos de ejemplo al iniciar la aplicación, solo si la base de datos está vacía.
 * Sirve para poder probar el cliente inmediatamente.
 * <p>
 * Los libros se crean con la FÁBRICA (cada creator decide físico o digital) y se arman con el
 * BUILDER que devuelve creator.builder(). Ya no hay new Libro(...).
 */
@Configuration
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner cargarDatosDeEjemplo(LocalidadRepository localidadRepository,
                                           AutorRepository autorRepository,
                                           PersonaRepository personaRepository,
                                           PersonaService personaService,
                                           LibroFisicoCreator fisico,
                                           LibroDigitalCreator digital) {
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

            // Persona 1: domicilio en Capital, dos libros (uno físico y uno digital)
            Persona ana = new Persona("Ana", "Martínez", 30123456,
                    new Domicilio("Av. San Martín", 1234, capital));
            ana.getLibros().add(fisico.builder()
                    .titulo("Cien años de soledad").fecha(1967).genero("Novela").paginas(471)
                    .autores(garciaMarquez)
                    .datosPropios(libro -> libro.setUbicacion("Estante A1"))
                    .build());
            ana.getLibros().add(digital.builder()
                    .titulo("Ficciones").fecha(1944).genero("Cuentos").paginas(174)
                    .autores(borges)
                    .datosPropios(libro -> libro.setFormato("EPUB"))
                    .build());
            personaService.create(ana);

            // Persona 2: domicilio en Godoy Cruz, dos libros (uno con dos autores)
            Persona luis = new Persona("Luis", "Fernández", 28456789,
                    new Domicilio("Calle Belgrano", 567, godoyCruz));
            luis.getLibros().add(fisico.builder()
                    .titulo("Rayuela").fecha(1963).genero("Novela").paginas(635)
                    .autores(cortazar)
                    .datosPropios(libro -> libro.setUbicacion("Estante B3"))
                    .build());
            luis.getLibros().add(digital.builder()
                    .titulo("La casa de los espíritus").fecha(1982).genero("Novela").paginas(433)
                    .autores(allende, garciaMarquez)
                    .datosPropios(libro -> libro.setFormato("PDF"))
                    .build());
            personaService.create(luis);

            log.info("Datos de ejemplo cargados: 6 localidades, 4 autores y 2 personas con sus libros.");
        };
    }
}
