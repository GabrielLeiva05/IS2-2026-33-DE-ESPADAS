package com.uncuyo.tp1_ej4;

import com.uncuyo.tp1_ej4.entities.Categoria;
import com.uncuyo.tp1_ej4.entities.Estudio;
import com.uncuyo.tp1_ej4.entities.Videojuego;
import com.uncuyo.tp1_ej4.repositories.RepositorioCategoria;
import com.uncuyo.tp1_ej4.repositories.RepositorioEstudio;
import com.uncuyo.tp1_ej4.repositories.RepositorioVideojuego;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Component
public class DatosPruebaLoader implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatosPruebaLoader.class);

    private final RepositorioCategoria repositorioCategoria;
    private final RepositorioEstudio repositorioEstudio;
    private final RepositorioVideojuego repositorioVideojuego;

    public DatosPruebaLoader(
            RepositorioCategoria repositorioCategoria,
            RepositorioEstudio repositorioEstudio,
            RepositorioVideojuego repositorioVideojuego) {
        this.repositorioCategoria = repositorioCategoria;
        this.repositorioEstudio = repositorioEstudio;
        this.repositorioVideojuego = repositorioVideojuego;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repositorioCategoria.count() > 0
                || repositorioEstudio.count() > 0
                || repositorioVideojuego.count() > 0) {
            logger.info("Se omite la carga de datos de prueba porque ya hay datos en la base.");
            return;
        }

        List<Categoria> categorias = repositorioCategoria.saveAll(List.of(
                nuevaCategoria("Acción"),
                nuevaCategoria("Sandbox"),
                nuevaCategoria("Rol")
        ));
        List<Estudio> estudios = repositorioEstudio.saveAll(List.of(
                nuevoEstudio("Rockstar Games"),
                nuevoEstudio("Mojang Studios"),
                nuevoEstudio("Blizzard Entertainment")
        ));

        repositorioVideojuego.saveAll(List.of(
                nuevoVideojuego("Grand Theft Auto V",
                        "Aventura de acción en mundo abierto en Los Santos.",
                        "GTAVI.png", 59.99f, (short) 25, LocalDate.of(2013, 9, 17),
                        estudios.get(0), categorias.get(0)),
                nuevoVideojuego("Minecraft",
                        "Explora y construye en un mundo abierto de bloques.",
                        "minecraft.png", 29.99f, (short) 40, LocalDate.of(2011, 11, 18),
                        estudios.get(1), categorias.get(1)),
                nuevoVideojuego("World of Warcraft",
                        "Aventura de rol multijugador en el mundo de Azeroth.",
                        "wow.png", 39.99f, (short) 15, LocalDate.of(2004, 11, 23),
                        estudios.get(2), categorias.get(2))
        ));

        logger.info("Se cargaron datos de prueba: 3 categorías, 3 estudios y 3 videojuegos.");
    }

    private static Categoria nuevaCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return categoria;
    }

    private static Estudio nuevoEstudio(String nombre) {
        Estudio estudio = new Estudio();
        estudio.setNombre(nombre);
        return estudio;
    }

    private static Videojuego nuevoVideojuego(
            String titulo,
            String descripcion,
            String imagen,
            float precio,
            short stock,
            LocalDate fechaLanzamiento,
            Estudio estudio,
            Categoria categoria) {
        Videojuego videojuego = new Videojuego();
        videojuego.setTitulo(titulo);
        videojuego.setDescripcion(descripcion);
        videojuego.setImagen(imagen);
        videojuego.setPrecio(precio);
        videojuego.setStock(stock);
        videojuego.setFechaLanzamiento(Date.from(
                fechaLanzamiento.atStartOfDay().toInstant(ZoneOffset.UTC)));
        videojuego.setActivo(true);
        videojuego.setEstudio(estudio);
        videojuego.setCategoria(categoria);
        return videojuego;
    }
}
