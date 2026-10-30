package com.competencia.blockazo.model.enums;

/** Indica como se dio de alta el usuario. */
public enum Proveedor {
    LOCAL,   // registro con correo y clave
    GOOGLE,  // OAuth2 con Google
    AUTH0    // OAuth2 con Auth0 (opcional)
}
