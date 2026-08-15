//Paquetes
package com.example.uml_con_orm.entity;

//Imports
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GenerationType;
import java.util.Date;

@Entity
public class Venta {
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int idVenta;
    private Date fecha;
    private Double precioTotal;
    private String estado;

    @ManyToOne @JoinColumn(name = "idCliente", nullable = true)
    private Cliente cliente;

    //Constructor vacío
    public Venta(){

    }

    //Constructor sin ID
    public Venta(Date fecha, Double precioTotal, String estado, Cliente cliente){
        this.fecha = fecha;
        this.precioTotal = precioTotal;
        this.estado = estado;
        this.cliente = cliente;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Double getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(Double precioTotal) {
        this.precioTotal = precioTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Cliente getCliente(){
        return cliente;
    }

    public void setCliente(Cliente cliente){
        this.cliente = cliente;
    }
}
