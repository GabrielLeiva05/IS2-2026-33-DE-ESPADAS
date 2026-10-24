//Paquetes
package com.example.uml_con_orm.view;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Imports
import com.example.uml_con_orm.entity.Venta;
import com.example.uml_con_orm.service.VentaService;



@RestController
@RequestMapping("/ventas")
public class VentaController {
    
    private final VentaService ventaService;

    public VentaController(VentaService ventaService){
        this.ventaService = ventaService;
    }

    //Alta de venta desde el lado de usuario
    @PostMapping("/{idCliente}")
    public Venta registrar(@RequestBody Venta venta, @PathVariable int idCliente) {
        return ventaService.registrarVenta(venta, idCliente);
    }

    //Baja de una venta desde el lado del usuario
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable int id) {
        ventaService.eliminar(id);
    }
    
    //Consultar todas las ventas desde el lado de usuario
    @GetMapping
    public List<Venta> listar() {
        return ventaService.listar();
    }

    //Consultar historial de ventas por cliente desde el lado de usuario
    @GetMapping("/cliente/{idCliente}")
    public List<Venta> historialPorCliente(@PathVariable int idCliente) {
        return ventaService.historialPorCliente(idCliente);
    }

    //Modificacion de una venta desde el lado de usuario
    @PutMapping("/{id}/{idCliente}")
    public Venta modificar(@PathVariable int id, @PathVariable int idCliente, @RequestBody Venta venta) {
        venta.setIdVenta(id);
        return ventaService.editar(venta, idCliente);
    }
}
