//Paquetes
package com.example.uml_con_orm.service;

//Imports
import org.springframework.stereotype.Service;
import com.example.uml_con_orm.repository.ClienteRepository;
import com.example.uml_con_orm.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.uml_con_orm.entity.Venta;
import com.example.uml_con_orm.entity.Cliente;
import java.util.List;

@Service
public class VentaService {
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private VentaRepository ventaRepo;

    public Venta registrarVenta(Venta v, int idCliente){
        Cliente c = clienteRepo.findById(idCliente).orElseThrow();
        v.setCliente(c);
        return ventaRepo.save(v);
    }

    public Venta editar(Venta v, int idCliente) {
        Cliente c = clienteRepo.findById(idCliente).orElseThrow();
        v.setCliente(c);
        return ventaRepo.save(v);
    }

    public void eliminar(int idCliente){
        ventaRepo.deleteById(idCliente);
    }
    
    public List<Venta> listar(){
        return ventaRepo.findAll();
    }
    
    public List<Venta> historialPorCliente(int idCliente){
        return ventaRepo.findByClienteIdCliente(idCliente);
    }
}
