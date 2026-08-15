//Paquetes
package com.example.uml_con_orm.repository;

//Imports
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.uml_con_orm.entity.Venta;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer>{
    List<Venta> findByClienteIdCliente(int idCliente);
}
