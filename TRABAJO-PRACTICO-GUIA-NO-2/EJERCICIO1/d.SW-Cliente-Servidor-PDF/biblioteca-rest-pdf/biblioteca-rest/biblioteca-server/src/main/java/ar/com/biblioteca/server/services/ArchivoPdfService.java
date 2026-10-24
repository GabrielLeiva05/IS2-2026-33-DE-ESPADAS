package ar.com.biblioteca.server.services;

import ar.com.biblioteca.server.exceptions.BadRequestException;
import ar.com.biblioteca.server.exceptions.ResourceNotFoundException;
import ar.com.biblioteca.server.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Responsable ÚNICO de la persistencia de los PDF de los libros en el disco del servidor.
 * <p>
 * Los archivos se guardan en la carpeta configurada en {@code biblioteca.storage.pdf-dir}
 * (por defecto {@code C:/biblioteca}) con el nombre {@code libro_<titulo>_<id>.pdf}.
 * <p>
 * Seguridad:
 * <ul>
 *   <li>El nombre del archivo lo genera el servidor (título normalizado + id); jamás se usa el nombre
 *       que envía el cliente, lo que evita <i>path traversal</i> ({@code ../..}).</li>
 *   <li>Se verifica que la ruta final siga dentro de la carpeta base.</li>
 *   <li>Se valida el contenido real (cabecera {@code %PDF-}), no solo la extensión o el Content-Type,
 *       que el cliente puede falsear. Y se limita el tamaño.</li>
 * </ul>
 */
@Service
public class ArchivoPdfService {

    private static final Logger log = LoggerFactory.getLogger(ArchivoPdfService.class);
    private static final byte[] CABECERA_PDF = {'%', 'P', 'D', 'F', '-'};
    private static final int LARGO_MAXIMO_TITULO_EN_NOMBRE = 60;

    private final Path carpetaBase;
    private final long tamanioMaximoBytes;

    public ArchivoPdfService(@Value("${biblioteca.storage.pdf-dir:C:/biblioteca}") String carpeta,
                             @Value("${biblioteca.storage.pdf-max-mb:20}") long tamanioMaximoMb) {
        this.carpetaBase = Paths.get(carpeta).toAbsolutePath().normalize();
        this.tamanioMaximoBytes = tamanioMaximoMb * 1024 * 1024;
        log.info("Los PDF de los libros se guardarán en: {}", carpetaBase);
    }

    /**
     * Valida que el archivo recibido sea un PDF utilizable. Se invoca ANTES de tocar la base de datos
     * para fallar rápido con un 400 claro.
     */
    public void validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("El archivo PDF está vacío o no fue enviado");
        }
        if (archivo.getSize() > tamanioMaximoBytes) {
            throw new BadRequestException("El PDF supera el tamaño máximo permitido de "
                    + (tamanioMaximoBytes / 1024 / 1024) + " MB");
        }
        byte[] inicio = new byte[CABECERA_PDF.length];
        try (InputStream in = archivo.getInputStream()) {
            int leidos = in.readNBytes(inicio, 0, inicio.length);
            if (leidos < CABECERA_PDF.length || !java.util.Arrays.equals(inicio, CABECERA_PDF)) {
                throw new BadRequestException("El archivo no es un PDF válido");
            }
        } catch (IOException e) {
            throw new StorageException("No se pudo leer el archivo recibido", e);
        }
    }

    /**
     * Guarda el PDF y devuelve el nombre de archivo generado (el que se registra en el libro).
     * Si ya existía un archivo con ese nombre se reemplaza.
     */
    public String guardar(MultipartFile archivo, String tituloLibro, Long libroId) {
        validar(archivo);
        String nombre = construirNombre(tituloLibro, libroId);
        Path destino = resolverSeguro(nombre);
        Path temporal = null;
        try {
            Files.createDirectories(carpetaBase);
            // Se escribe primero a un temporal y luego se mueve: nunca queda un PDF a medio escribir.
            temporal = Files.createTempFile(carpetaBase, "subida_", ".tmp");
            try (InputStream in = archivo.getInputStream()) {
                Files.copy(in, temporal, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            log.info("PDF guardado: {}", destino);
            return nombre;
        } catch (IOException e) {
            throw new StorageException("No se pudo guardar el PDF en " + carpetaBase, e);
        } finally {
            if (temporal != null) {
                try {
                    Files.deleteIfExists(temporal);
                } catch (IOException ignorada) {
                    // best effort: el temporal solo existe si falló antes del move
                }
            }
        }
    }

    /** Devuelve el archivo para enviarlo como respuesta HTTP. 404 si no existe en disco. */
    public Resource cargar(String nombreArchivo) {
        Path ruta = resolverSeguro(nombreArchivo);
        if (!Files.isRegularFile(ruta) || !Files.isReadable(ruta)) {
            throw new ResourceNotFoundException(
                    "El archivo PDF del libro no se encuentra en el servidor (" + nombreArchivo + ")");
        }
        return new PathResource(ruta);
    }

    /** Borra el archivo si existe. No lanza excepción: un archivo huérfano no debe romper una baja. */
    public void eliminar(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolverSeguro(nombreArchivo));
        } catch (IOException | RuntimeException e) {
            log.warn("No se pudo eliminar el PDF {}: {}", nombreArchivo, e.getMessage());
        }
    }

    // ------------------------------------------------------------------ auxiliares

    /** libro_<titulo normalizado>_<id>.pdf. El id evita que dos libros con el mismo título se pisen. */
    String construirNombre(String titulo, Long libroId) {
        return "libro_" + normalizar(titulo) + "_" + libroId + ".pdf";
    }

    /** Minúsculas, sin tildes y solo [a-z0-9_]; "Cien años de soledad" -> "cien_anos_de_soledad". */
    static String normalizar(String texto) {
        if (texto == null) {
            return "sin_titulo";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        String limpio = sinTildes.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (limpio.length() > LARGO_MAXIMO_TITULO_EN_NOMBRE) {
            limpio = limpio.substring(0, LARGO_MAXIMO_TITULO_EN_NOMBRE).replaceAll("_+$", "");
        }
        return limpio.isEmpty() ? "sin_titulo" : limpio;
    }

    /** Resuelve el nombre dentro de la carpeta base y rechaza cualquier ruta que se escape de ella. */
    private Path resolverSeguro(String nombreArchivo) {
        Path ruta = carpetaBase.resolve(nombreArchivo).normalize();
        if (!ruta.startsWith(carpetaBase) || ruta.equals(carpetaBase)) {
            throw new BadRequestException("Nombre de archivo no permitido");
        }
        return ruta;
    }
}
