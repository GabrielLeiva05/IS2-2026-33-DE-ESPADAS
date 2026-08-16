package com.laboratorio.ejerciciog.dao;

import com.laboratorio.ejerciciog.dto.ProductoRequest;
import com.laboratorio.ejerciciog.dto.ProductoResponse;

import java.util.List;

public interface ProductoDAO {
    ProductoResponse findById(Integer codigo);
    List<ProductoResponse> findAll();
    void save(ProductoRequest request);
    boolean update(Integer codigo, ProductoRequest request);
    boolean delete(Integer codigo);
}