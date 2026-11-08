package com.dulcemayor.admin.dto;

/** Formulario con dirección en selects encadenados (empleados, proveedores). Lo usa ServicioUbicacion. */
public interface FormularioConUbicacion {

    String getIdPais();

    void setIdPais(String idPais);

    String getIdProvincia();

    void setIdProvincia(String idProvincia);

    String getIdDepartamento();

    String getIdLocalidad();
}
