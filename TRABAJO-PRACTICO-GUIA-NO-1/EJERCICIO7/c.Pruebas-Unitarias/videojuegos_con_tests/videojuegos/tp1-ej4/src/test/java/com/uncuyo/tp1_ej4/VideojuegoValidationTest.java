package com.uncuyo.tp1_ej4;

import com.uncuyo.tp1_ej4.entities.Categoria;
import com.uncuyo.tp1_ej4.entities.Estudio;
import com.uncuyo.tp1_ej4.entities.Videojuego;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Pruebas unitarias de las restricciones de Jakarta Bean Validation
 * declaradas en la entidad Videojuego (@NotEmpty, @Size, @Min, @Max,
 * @NotNull, @PastOrPresent). No dependen de Spring ni de una base de
 * datos: usan directamente el Validator de Hibernate Validator, que ya
 * esta en el classpath via spring-boot-starter-validation. Complementan a
 * ServicioVideojuegoTest, que cubre la logica de negocio pero no valida
 * que las anotaciones esten bien configuradas.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VideojuegoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private Videojuego videojuego;

    @BeforeAll
    public static void beforeAll() {
        System.out.println("beforeAll VideojuegoValidationTest");
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("afterAll VideojuegoValidationTest");
        factory.close();
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        System.out.println("beforeEach " + testInfo.getDisplayName());

        Estudio estudio = new Estudio();
        estudio.setId(1L);
        estudio.setNombre("CD Projekt Red");

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("RPG");

        videojuego = new Videojuego();
        videojuego.setId(1L);
        videojuego.setTitulo("The Witcher 3");
        videojuego.setDescripcion("Un RPG de mundo abierto");
        videojuego.setPrecio(2999.99f);
        videojuego.setStock((short) 50);
        videojuego.setFechaLanzamiento(new Date());
        videojuego.setActivo(true);
        videojuego.setEstudio(estudio);
        videojuego.setCategoria(categoria);
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        System.out.println("afterEach " + testInfo.getDisplayName());
    }

    private Date fechaRelativaADiasDeHoy(int dias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, dias);
        return cal.getTime();
    }

    @Test
    @Order(1)
    public void testVideojuegoValidoNoTieneViolaciones() {
        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.isEmpty());

        System.out.println("resultado violaciones=" + violaciones.size());
    }

    @Test
    @Order(2)
    public void testTituloVacioGeneraViolacion() {
        videojuego.setTitulo("");

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertFalse(violaciones.isEmpty());
        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));

        System.out.println("resultado " + violaciones.size() + " violaciones");
    }

    @Test
    @Order(3)
    public void testDescripcionMuyCortaGeneraViolacion() {
        videojuego.setDescripcion("Hi"); // menos de 5 caracteres

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("descripcion")));
    }

    @Test
    @Order(4)
    public void testDescripcionMuyLargaGeneraViolacion() {
        videojuego.setDescripcion("a".repeat(101)); // mas de 100 caracteres

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("descripcion")));
    }

    @Test
    @Order(5)
    public void testPrecioMenorAlMinimoGeneraViolacion() {
        videojuego.setPrecio(4.99f); // el minimo es 5

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("precio")));
    }

    @Test
    @Order(6)
    public void testPrecioMayorAlMaximoGeneraViolacion() {
        videojuego.setPrecio(10000.01f); // el maximo es 10000

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("precio")));
    }

    @Test
    @Order(7)
    public void testStockMenorAlMinimoGeneraViolacion() {
        videojuego.setStock((short) 0); // el minimo es 1

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("stock")));
    }

    @Test
    @Order(8)
    public void testStockMayorAlMaximoGeneraViolacion() {
        videojuego.setStock((short) 10001); // el maximo es 10000

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("stock")));
    }

    @Test
    @Order(9)
    public void testFechaLanzamientoFuturaGeneraViolacion() {
        videojuego.setFechaLanzamiento(fechaRelativaADiasDeHoy(30)); // 30 dias en el futuro

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("fechaLanzamiento")));
    }

    @Test
    @Order(10)
    public void testFechaLanzamientoNulaGeneraViolacion() {
        videojuego.setFechaLanzamiento(null);

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("fechaLanzamiento")));
    }

    @Test
    @Order(11)
    public void testEstudioNuloGeneraViolacion() {
        videojuego.setEstudio(null);

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("estudio")));
    }

    @Test
    @Order(12)
    public void testCategoriaNulaGeneraViolacion() {
        videojuego.setCategoria(null);

        Set<ConstraintViolation<Videojuego>> violaciones = validator.validate(videojuego);

        Assertions.assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("categoria")));
    }
}
