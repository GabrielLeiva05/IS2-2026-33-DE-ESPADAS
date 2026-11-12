package com.dulcemayor.admin.dto;

/** Formato de lectura de las fechas ISO que devuelve la API ("2026-11-03T10:15:30" -> "03/11/2026 10:15"). */
final class FechasLegibles {

    private FechasLegibles() {
    }

    static String fecha(String iso) {
        if (iso == null || iso.length() < 10) {
            return "";
        }
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4);
    }

    static String fechaHora(String iso) {
        if (iso == null || iso.length() < 16) {
            return fecha(iso);
        }
        return fecha(iso) + " " + iso.substring(11, 16);
    }
}
