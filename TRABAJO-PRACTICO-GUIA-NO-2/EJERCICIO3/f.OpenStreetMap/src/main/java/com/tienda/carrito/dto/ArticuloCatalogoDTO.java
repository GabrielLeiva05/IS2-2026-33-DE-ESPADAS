package com.tienda.carrito.dto;

/** Datos livianos de un articulo para mostrar en el catalogo (sin cargar los bytes de las imagenes). */
public class ArticuloCatalogoDTO {

    private final String id;
    private final String nombre;
    private final double precio;
    private final String proveedor;
    private final String imagenId;

    public ArticuloCatalogoDTO(String id, String nombre, double precio, String proveedor, String imagenId) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.proveedor = proveedor;
        this.imagenId = imagenId;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public String getProveedor() { return proveedor; }
    /** Id de la primera imagen del articulo (null si no tiene). */
    public String getImagenId() { return imagenId; }
}
