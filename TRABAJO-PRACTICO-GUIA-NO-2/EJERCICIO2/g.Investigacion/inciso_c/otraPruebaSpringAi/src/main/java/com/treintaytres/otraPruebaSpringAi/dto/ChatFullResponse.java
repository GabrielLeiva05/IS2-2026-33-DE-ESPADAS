package com.treintaytres.otraPruebaSpringAi.dto;

public record ChatFullResponse(    String content,
                                   Long promptTokens,
                                   Long completionTokens,
                                   Long totalTokens) {


}
