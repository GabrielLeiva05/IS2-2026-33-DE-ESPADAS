package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Categoría del catálogo. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoriaDTO {

    private String id;
    private String nombre;

    /** Ícono del sprite según el nombre de la categoría (solo presentación). */
    public String getIcono() {
        String texto = nombre == null ? "" : nombre.toLowerCase(java.util.Locale.ROOT);
        if (texto.contains("chocolat")) {
            return "i-chocolate";
        }
        if (texto.contains("gomit") || texto.contains("goma")) {
            return "i-gomita";
        }
        if (texto.contains("chupet") || texto.contains("paleta")) {
            return "i-chupetin";
        }
        if (texto.contains("caramel") || texto.contains("pastill")) {
            return "i-caramelo";
        }
        if (texto.contains("combo") || texto.contains("surtid") || texto.contains("caja")) {
            return "i-combo";
        }
        return "i-estrella";
    }
}
