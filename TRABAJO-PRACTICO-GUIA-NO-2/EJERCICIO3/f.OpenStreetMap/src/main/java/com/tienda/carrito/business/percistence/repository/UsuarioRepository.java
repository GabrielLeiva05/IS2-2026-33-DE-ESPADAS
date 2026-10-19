package com.tienda.carrito.business.percistence.repository;

import com.tienda.carrito.business.domain.entity.Rol;
import com.tienda.carrito.business.domain.entity.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends BaseRepository<Usuario, String> {

    /** Usuario activo con ese nombre (sin distinguir mayusculas) o null. */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) = LOWER(:nombre) AND u.eliminado = FALSE")
    Usuario buscarUsuarioPorNombre(@Param("nombre") String nombre);

    /** Cantidad de usuarios activos con un rol (para proteger al ultimo administrador). */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol AND u.eliminado = FALSE")
    long contarUsuariosActivosPorRol(@Param("rol") Rol rol);
}
