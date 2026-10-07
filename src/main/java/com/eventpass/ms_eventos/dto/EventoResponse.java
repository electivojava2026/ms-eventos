package com.eventpass.ms_eventos.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventoResponse(
        Long id,
        String nombre,
        String descripcion,
        String lugar,
        LocalDateTime fechaEvento,
        List<TipoEntradaResponse> tiposEntrada) {
}
