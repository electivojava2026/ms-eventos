package com.eventpass.ms_eventos.dto;

import java.math.BigDecimal;

public record TipoEntradaResponse(Long id, String nombre, BigDecimal precio, Integer aforo, Integer aforoDisponible) {
}
