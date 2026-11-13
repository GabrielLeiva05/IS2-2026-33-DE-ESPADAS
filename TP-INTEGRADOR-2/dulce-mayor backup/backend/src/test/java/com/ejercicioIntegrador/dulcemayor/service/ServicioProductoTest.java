package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ProductoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Categoria;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.SubCategoria;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioProducto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioProductoTest {

    private RepositorioProducto repositorio;
    private ServicioSubCategoria servicioSubCategoria;
    private ServicioImagen servicioImagen;
    private ServicioProducto servicio;
    private SubCategoria alfajores;

    @BeforeEach
    void setUp() throws MiException {
        repositorio = mock(RepositorioProducto.class);
        servicioSubCategoria = mock(ServicioSubCategoria.class);
        servicioImagen = mock(ServicioImagen.class);
        servicio = new ServicioProducto(repositorio, servicioSubCategoria, servicioImagen);

        alfajores = Datos.subCategoria("sub-1", "Alfajores", Datos.categoria("cat-1", "Golosinas"));
        when(servicioSubCategoria.obtenerEntidad("sub-1")).thenReturn(alfajores);
        when(servicioImagen.obtenerEntidad("img-1")).thenReturn(Datos.imagen("img-1", TipoImagen.PRODUCTO));
        when(repositorio.findFirstByCodigoIgnoreCaseAndEliminadoFalse(any())).thenReturn(Optional.empty());
        when(repositorio.findByImagenes_Id(any())).thenReturn(List.of());
        when(repositorio.save(any(Producto.class))).then(returnsFirstArg());
    }

    private static ProductoRequestDTO pedidoValido() {
        return new ProductoRequestDTO("ALF-001", "Alfajor triple", "Caja x 24", Presentacion.CAJA, 24, false,
                "sub-1", List.of("img-1"));
    }

    @Test
    void altaCreaElProductoConSuClasificacionEImagenes() throws MiException {
        ProductoResponseDTO creado = servicio.alta(pedidoValido());

        assertEquals("ALF-001", creado.codigo());
        assertEquals("Alfajores", creado.nombreSubCategoria());
        assertEquals("Golosinas", creado.nombreCategoria());
        assertEquals(1, creado.imagenes().size());
    }

    @Test
    void altaRechazaUnCodigoRepetido() {
        Producto existente = Datos.producto("prod-9", "ALF-001", "Otro", alfajores);
        when(repositorio.findFirstByCodigoIgnoreCaseAndEliminadoFalse("ALF-001")).thenReturn(Optional.of(existente));

        MiException error = assertThrows(MiException.class, () -> servicio.alta(pedidoValido()));

        assertEquals("Ya existe un producto con el código ALF-001", error.getMessage());
        verify(repositorio, never()).save(any(Producto.class));
    }

    @Test
    void altaRechazaUnProductoSinImagenes() {
        ProductoRequestDTO sinImagenes = new ProductoRequestDTO("ALF-001", "Alfajor triple", null, Presentacion.CAJA,
                24, false, "sub-1", List.of());

        MiException error = assertThrows(MiException.class, () -> servicio.alta(sinImagenes));

        assertEquals("El producto debe tener al menos una imagen", error.getMessage());
    }

    @Test
    void altaRechazaUnaVentaPorUnidadConVariasUnidades() {
        ProductoRequestDTO unidad = new ProductoRequestDTO("CHU-01", "Chupetín", null, Presentacion.UNIDAD, 6, false,
                "sub-1", List.of("img-1"));

        assertThrows(MiException.class, () -> servicio.alta(unidad));
    }

    @Test
    void altaRechazaUnaImagenQueNoEsDeProducto() throws MiException {
        when(servicioImagen.obtenerEntidad("img-2")).thenReturn(Datos.imagen("img-2", TipoImagen.SUCURSAL));
        ProductoRequestDTO conImagenDeSucursal = new ProductoRequestDTO("ALF-001", "Alfajor triple", null,
                Presentacion.CAJA, 24, false, "sub-1", List.of("img-2"));

        assertThrows(MiException.class, () -> servicio.alta(conImagenDeSucursal));
    }

    @Test
    void unaSubcategoriaInexistenteSeInformaComoDatoInvalidoYNoComo404() throws MiException {
        when(servicioSubCategoria.obtenerEntidad("sub-x")).thenThrow(new RecursoNoEncontradoException("no existe"));
        ProductoRequestDTO conSubcategoriaInexistente = new ProductoRequestDTO("ALF-001", "Alfajor triple", null,
                Presentacion.CAJA, 24, false, "sub-x", List.of("img-1"));

        MiException error = assertThrows(MiException.class, () -> servicio.alta(conSubcategoriaInexistente));

        assertFalse(error instanceof RecursoNoEncontradoException);
    }

    @Test
    void altaRechazaUnaSubcategoriaDeUnaCategoriaDadaDeBaja() throws MiException {
        Categoria eliminada = Datos.categoria("cat-2", "Discontinuados");
        eliminada.setEliminado(true);
        when(servicioSubCategoria.obtenerEntidad("sub-2"))
                .thenReturn(Datos.subCategoria("sub-2", "Viejos", eliminada));
        ProductoRequestDTO dto = new ProductoRequestDTO("ALF-001", "Alfajor triple", null, Presentacion.CAJA, 24,
                false, "sub-2", List.of("img-1"));

        assertThrows(MiException.class, () -> servicio.alta(dto));
    }

    @Test
    void altaRechazaUnaImagenQueYaPerteneceAOtroProducto() {
        Producto otro = Datos.producto("prod-9", "ALF-009", "Otro alfajor", alfajores);
        when(repositorio.findByImagenes_Id("img-1")).thenReturn(List.of(otro));

        MiException error = assertThrows(MiException.class, () -> servicio.alta(pedidoValido()));

        assertEquals("La imagen img-1.png ya está asignada al producto ALF-009", error.getMessage());
    }

    @Test
    void modificarNoPermiteCambiarElCodigo() {
        Producto existente = Datos.producto("prod-1", "ALF-001", "Alfajor triple", alfajores);
        when(repositorio.findByIdAndEliminadoFalse("prod-1")).thenReturn(Optional.of(existente));
        ProductoRequestDTO otroCodigo = new ProductoRequestDTO("ALF-999", "Alfajor triple", null, Presentacion.CAJA,
                24, false, "sub-1", null);

        MiException error = assertThrows(MiException.class, () -> servicio.modificar("prod-1", otroCodigo));

        assertEquals("El código de un producto no se puede modificar", error.getMessage());
    }

    @Test
    void modificarSinIdsDeImagenConservaLasImagenesActuales() throws MiException {
        Producto existente = Datos.producto("prod-1", "ALF-001", "Alfajor triple", alfajores);
        when(repositorio.findByIdAndEliminadoFalse("prod-1")).thenReturn(Optional.of(existente));
        when(repositorio.findFirstByCodigoIgnoreCaseAndEliminadoFalse("ALF-001")).thenReturn(Optional.of(existente));
        ProductoRequestDTO dto = new ProductoRequestDTO(null, "Alfajor triple negro", null, Presentacion.CAJA, 24,
                true, "sub-1", null);

        ProductoResponseDTO modificado = servicio.modificar("prod-1", dto);

        assertEquals("Alfajor triple negro", modificado.nombre());
        assertEquals(1, modificado.imagenes().size());
    }
}
