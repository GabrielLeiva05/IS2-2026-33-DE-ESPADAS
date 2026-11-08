package com.ejercicioIntegrador.dulcemayor.dto;

import java.time.LocalDateTime;

public record SuscripcionNewsletterResponseDTO(String id, String email, boolean activa, String tokenBaja,
                                               LocalDateTime fechaAlta, LocalDateTime fechaBaja,
                                               LocalDateTime fechaUltimoEnvio) {
}
