package com.tienda.carrito.config;

import com.tienda.carrito.business.domain.entity.Articulo;
import com.tienda.carrito.business.domain.entity.Proveedor;
import com.tienda.carrito.business.domain.entity.Rol;
import com.tienda.carrito.business.domain.entity.Usuario;
import com.tienda.carrito.business.logic.service.ArticuloService;
import com.tienda.carrito.business.logic.service.ProveedorService;
import com.tienda.carrito.business.logic.service.UsuarioService;
import com.tienda.carrito.dto.ArchivoImagen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Carga datos de ejemplo al iniciar. Usa SERVICIOS (no repositorios), asi tambien se aplican las reglas de negocio.
 *  - Garantiza que exista un ADMIN (admin / admin123). En una base creada con la version anterior (sin roles),
 *    el usuario "admin" existente se promueve a ADMIN.
 *  - Crea un CLIENTE de prueba (cliente / cliente123) si no existe.
 *  - Con la base de productos vacia, carga 2 proveedores y 6 articulos con imagen.
 * Se desactiva con app.seed.enabled=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UsuarioService usuarioService;
    private final ProveedorService proveedorService;
    private final ArticuloService articuloService;

    public DataLoader(UsuarioService usuarioService, ProveedorService proveedorService, ArticuloService articuloService) {
        this.usuarioService = usuarioService;
        this.proveedorService = proveedorService;
        this.articuloService = articuloService;
    }

    @Override
    public void run(String... args) throws Exception {
        usuarioService.asegurarAdministrador("admin", "admin123");

        if (!existeUsuario("cliente")) {
            Usuario cliente = new Usuario();
            cliente.setNombre("cliente");
            cliente.setClave("cliente123");
            cliente.setRol(Rol.CLIENTE);
            usuarioService.alta(cliente);
            log.info("Usuarios de ejemplo: admin / admin123 (ADMIN) y cliente / cliente123 (CLIENTE)");
        }

        if (!proveedorService.listarActivos().isEmpty()) {
            return; // ya hay datos: no se vuelve a cargar nada
        }

        Proveedor andina = crearProveedor("Distribuidora Andina");
        Proveedor cuyo = crearProveedor("Bodega y Regalos Cuyo");

        crearArticulo("Mochila urbana", 45900.00, andina, new Color(33, 37, 41), new Color(108, 117, 125));
        crearArticulo("Auriculares inalámbricos", 78500.50, andina, new Color(13, 110, 253), new Color(102, 16, 242));
        crearArticulo("Taza de cerámica", 6800.00, cuyo, new Color(214, 51, 132), new Color(253, 126, 20));
        crearArticulo("Cuaderno de tapa dura", 5200.00, cuyo, new Color(25, 135, 84), new Color(32, 201, 151));
        crearArticulo("Botella térmica 750 ml", 18900.00, andina, new Color(220, 53, 69), new Color(255, 193, 7));
        crearArticulo("Set de vasos de vidrio", 12400.00, cuyo, new Color(13, 202, 240), new Color(13, 110, 253));

        log.info("Datos de ejemplo cargados: 2 proveedores y 6 articulos.");
    }

    private boolean existeUsuario(String nombre) throws Exception {
        return usuarioService.listarActivos().stream().anyMatch(u -> u.getNombre().equalsIgnoreCase(nombre));
    }

    private Proveedor crearProveedor(String nombre) throws Exception {
        Proveedor p = new Proveedor();
        p.setNombre(nombre);
        return proveedorService.alta(p);
    }

    private void crearArticulo(String nombre, double precio, Proveedor proveedor, Color c1, Color c2) throws Exception {
        Articulo a = new Articulo();
        a.setNombre(nombre);
        a.setPrecio(precio);
        a.setProveedor(proveedor);
        ArchivoImagen imagen = new ArchivoImagen(nombre.toLowerCase().replace(' ', '-') + ".png", "image/png", generarPng(c1, c2));
        articuloService.altaConImagenes(a, List.of(imagen));
    }

    /** Genera un PNG de 450x300 con un degradado y unos circulos (sin texto, para no depender de fuentes del sistema). */
    private byte[] generarPng(Color c1, Color c2) {
        BufferedImage imagen = new BufferedImage(450, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, c1, 450, 300, c2));
            g.fillRect(0, 0, 450, 300);
            g.setColor(new Color(255, 255, 255, 70));
            g.fillOval(140, 60, 170, 170);
            g.setColor(new Color(255, 255, 255, 110));
            g.fillOval(190, 110, 70, 70);
        } finally {
            g.dispose();
        }
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "png", salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
