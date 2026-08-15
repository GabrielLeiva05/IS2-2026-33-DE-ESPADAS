//Paquetes
package com.example.uml_con_orm.service;

//Imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.uml_con_orm.repository.ClienteRepository;
import com.example.uml_con_orm.repository.VentaRepository;
import com.example.uml_con_orm.entity.Cliente;
import com.example.uml_con_orm.exception.ClienteConVentasException;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private VentaRepository ventaRepo;
    
    public Cliente registrar(Cliente c){
        return clienteRepo.save(c);
    }

    public Cliente editar(Cliente c){
        return clienteRepo.save(c);
    }

    public void eliminar(int idCliente){
        boolean tieneVentas = !ventaRepo.findByClienteIdCliente(idCliente).isEmpty();
        if (tieneVentas) {
            throw new ClienteConVentasException(
                "No se puede eliminar el cliente porque tiene ventas asociadas."
            );
        }
        clienteRepo.deleteById(idCliente);
    }

    public List<Cliente> listar(){
        return clienteRepo.findAll();
    }

    public Optional<Cliente> buscarPorId(int idCliente){
        return clienteRepo.findById(idCliente);
    }
}
