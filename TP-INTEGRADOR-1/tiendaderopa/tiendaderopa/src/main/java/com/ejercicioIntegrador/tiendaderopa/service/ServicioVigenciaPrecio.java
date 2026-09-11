package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.VigenciaPrecio;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioProducto;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioVigenciaPrecio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;

@Service
public class ServicioVigenciaPrecio {

    @Autowired
    private RepositorioVigenciaPrecio repositorio;
    @Autowired
    private RepositorioProducto repositorioProducto;

    public void validar(LocalDate fechaDesde, LocalDate fechaHasta, double precio, String idProducto) throws Exception {
        if (fechaDesde == null) throw new Exception("La fecha desde es obligatoria");
        if (precio <= 0) throw new Exception("El precio debe ser mayor a cero");
        if (fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            throw new Exception("La fecha hasta no puede ser anterior a la fecha desde");
        }

        repositorioProducto.findById(idProducto)
                .orElseThrow(() -> new Exception("No existe el producto con id " + idProducto));

        VigenciaPrecio vigente = buscarVigenciaPrecioVigente(idProducto);
        if (vigente != null && fechaDesde.isBefore(vigente.getFechaDesde())) {
            throw new Exception("La nueva vigencia no puede iniciar antes que la vigencia actual ("
                    + vigente.getFechaDesde() + ")");
        }
    }

    @Transactional
    public void crearVigenciaPrecio(LocalDate fechaDesde, LocalDate fechaHasta, double precio, String idProducto) throws Exception {
        validar(fechaDesde, fechaHasta, precio, idProducto);

        Producto producto = repositorioProducto.findById(idProducto)
                .orElseThrow(() -> new Exception("No existe el producto con id " + idProducto));

        // Cierra la vigencia anterior, si existe
        VigenciaPrecio vigenteActual = buscarVigenciaPrecioVigente(idProducto);
        if (vigenteActual != null) {
            vigenteActual.setFechaHasta(fechaDesde);
            repositorio.save(vigenteActual);
        }

        VigenciaPrecio nueva = new VigenciaPrecio();
        nueva.setFechaDesde(fechaDesde);
        nueva.setFechaHasta(fechaHasta);
        nueva.setPrecio(precio);
        nueva.setEliminado(false);
        nueva.setProducto(producto);
        repositorio.save(nueva);
    }

    public VigenciaPrecio buscarVigenciaPrecio(String id) throws Exception {
        return repositorio.findById(id)
                .orElseThrow(() -> new Exception("No existe la vigencia de precio con id " + id));
    }

    @Transactional
    public void modificarVigenciaPrecio(String id, LocalDate fechaDesde, LocalDate fechaHasta, double precio, String idProducto) throws Exception {
        VigenciaPrecio vigencia = buscarVigenciaPrecio(id);
        validar(fechaDesde, fechaHasta, precio, idProducto);

        Producto producto = repositorioProducto.findById(idProducto)
                .orElseThrow(() -> new Exception("No existe el producto con id " + idProducto));

        vigencia.setFechaDesde(fechaDesde);
        vigencia.setFechaHasta(fechaHasta);
        vigencia.setPrecio(precio);
        vigencia.setProducto(producto);
        repositorio.save(vigencia);
    }

    @Transactional
    public void eliminarVigenciaPrecio(String id) throws Exception {
        VigenciaPrecio vigencia = buscarVigenciaPrecio(id);
        vigencia.setEliminado(true); // baja lógica
        repositorio.save(vigencia);
    }

    public Collection<VigenciaPrecio> listarVigenciaPrecio() {
        return repositorio.findAll();
    }

    public Collection<VigenciaPrecio> listarVigenciaPrecioActivo() {
        return repositorio.findByEliminadoFalse();
    }

    public VigenciaPrecio buscarVigenciaPrecioVigente(String idProducto) {
        return repositorio.findByProducto_IdAndFechaHastaIsNull(idProducto).orElse(null);
    }
}