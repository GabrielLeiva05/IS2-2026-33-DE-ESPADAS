//Paquetes
package com.example.uml_con_orm.view;

//Imports
import com.example.uml_con_orm.entity.Cliente;
import com.example.uml_con_orm.service.ClienteService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/clientes")
public class ClienteController {
    
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService){
        this.clienteService = clienteService;
    }

    //Alta de cliente desde el lado de usuario
    @PostMapping
    public Cliente crear(@RequestBody Cliente cliente){
        return clienteService.registrar(cliente);
    }

    //Modificacion de cliente desde el lado de usuario
    @PutMapping("/{id}")
    public Cliente modificar(@PathVariable int id, @RequestBody Cliente cliente){
        cliente.setIdCliente(id);
        return clienteService.editar(cliente);
    }

    //Baja desde el lado del usuario
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable int id) {
       clienteService.eliminar(id);
    }

    //Consultar todos desde el lado de usuario
    @GetMapping
    public List<Cliente> listar() {
        return clienteService.listar();
    }

    //Consultar por id desde el lado de usuario
    @GetMapping("/{id}")
    public Optional<Cliente> buscarUno(@PathVariable int id) {
        return clienteService.buscarPorId(id);
    }
}
