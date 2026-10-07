package com.eventpass.ms_eventos.dto;

import java.math.BigDecimal;

public record TipoEntradaRequest(String nombre, BigDecimal precio, Integer aforo) {
}
