//Paquetes
package com.example.uml_con_orm.repository;

//Imports
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.uml_con_orm.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer>{
    
}