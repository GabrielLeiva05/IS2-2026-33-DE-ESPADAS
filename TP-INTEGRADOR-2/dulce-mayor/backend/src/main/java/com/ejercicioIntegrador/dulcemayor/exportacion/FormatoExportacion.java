package com.ejercicioIntegrador.dulcemayor.exportacion;

/** Formatos de archivo en los que se pueden exportar los reportes. */
public enum FormatoExportacion {
    PDF("pdf", "application/pdf"),
    EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final String extension;
    private final String tipoContenido;

    FormatoExportacion(String extension, String tipoContenido) {
        this.extension = extension;
        this.tipoContenido = tipoContenido;
    }

    public String getExtension() {
        return extension;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }
}
