package com.eventpass.ms_eventos.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventoRequest(
        String nombre,
        String descripcion,
        String lugar,
        LocalDateTime fechaEvento,
        List<TipoEntradaRequest> tiposEntrada) {
}
