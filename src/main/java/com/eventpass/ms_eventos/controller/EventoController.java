package com.eventpass.ms_eventos.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventpass.ms_eventos.dto.EventoRequest;
import com.eventpass.ms_eventos.dto.EventoResponse;
import com.eventpass.ms_eventos.dto.ReservaRequest;
import com.eventpass.ms_eventos.dto.TipoEntradaRequest;
import com.eventpass.ms_eventos.dto.TipoEntradaResponse;
import com.eventpass.ms_eventos.service.EventoService;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    // --- Consulta: USER y STAFF ---

    @GetMapping
    public ResponseEntity<List<EventoResponse>> listar() {
        return ResponseEntity.ok(eventoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.obtener(id));
    }

    // --- Gestion: solo STAFF (ver SecurityConfig) ---

    @PostMapping
    public ResponseEntity<EventoResponse> crear(@RequestBody EventoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponse> actualizar(@PathVariable Long id, @RequestBody EventoRequest request) {
        return ResponseEntity.ok(eventoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eventoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventoId}/tipos-entrada")
    public ResponseEntity<TipoEntradaResponse> agregarTipoEntrada(@PathVariable Long eventoId,
            @RequestBody TipoEntradaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.agregarTipoEntrada(eventoId, request));
    }

    // Verifica y descuenta aforo; responde 409 si no quedan cupos
    @PostMapping("/{eventoId}/tipos-entrada/{tipoId}/reservas")
    public ResponseEntity<TipoEntradaResponse> reservarAforo(@PathVariable Long eventoId,
            @PathVariable Long tipoId, @RequestBody ReservaRequest request) {
        return ResponseEntity.ok(eventoService.reservarAforo(eventoId, tipoId, request.cantidad()));
    }
}
