package com.eventpass.ms_eventos.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventpass.ms_eventos.dto.EventoRequest;
import com.eventpass.ms_eventos.dto.EventoResponse;
import com.eventpass.ms_eventos.dto.TipoEntradaRequest;
import com.eventpass.ms_eventos.dto.TipoEntradaResponse;
import com.eventpass.ms_eventos.exception.RecursoNoEncontradoException;
import com.eventpass.ms_eventos.exception.SinAforoException;
import com.eventpass.ms_eventos.exception.SolicitudInvalidaException;
import com.eventpass.ms_eventos.model.Evento;
import com.eventpass.ms_eventos.model.TipoEntrada;
import com.eventpass.ms_eventos.repository.EventoRepository;
import com.eventpass.ms_eventos.repository.TipoEntradaRepository;

@Service
@Transactional
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TipoEntradaRepository tipoEntradaRepository;

    // ---------- CRUD de eventos ----------

    // C - crear (puede traer sus tipos de entrada)
    public EventoResponse crear(EventoRequest request) {
        validarEvento(request);
        if (request.tiposEntrada() != null) {
            request.tiposEntrada().forEach(this::validarTipo);
        }

        Evento evento = new Evento();
        aplicarDatos(evento, request);
        if (request.tiposEntrada() != null) {
            for (TipoEntradaRequest t : request.tiposEntrada()) {
                evento.agregarTipoEntrada(construirTipo(t));
            }
        }
        return aRespuesta(eventoRepository.save(evento));
    }

    // R - leer todos
    @Transactional(readOnly = true)
    public List<EventoResponse> listar() {
        return eventoRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    // R - leer uno
    @Transactional(readOnly = true)
    public EventoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    // U - actualizar datos del evento (no toca los tipos de entrada ni su aforo)
    public EventoResponse actualizar(Long id, EventoRequest request) {
        validarEvento(request);
        Evento evento = buscar(id);
        aplicarDatos(evento, request);
        return aRespuesta(eventoRepository.save(evento));
    }

    // D - eliminar (borra tambien sus tipos de entrada)
    public void eliminar(Long id) {
        eventoRepository.delete(buscar(id));
    }

    // ---------- Tipos de entrada y aforo ----------

    public TipoEntradaResponse agregarTipoEntrada(Long eventoId, TipoEntradaRequest request) {
        validarTipo(request);
        Evento evento = buscar(eventoId);
        TipoEntrada tipo = construirTipo(request);
        evento.agregarTipoEntrada(tipo);
        return aRespuestaTipo(tipoEntradaRepository.save(tipo));
    }

    /**
     * Verifica y descuenta aforo (lo usara el proceso asincrono de emision de tickets).
     * Lanza SinAforoException (409) si no quedan cupos suficientes.
     */
    public TipoEntradaResponse reservarAforo(Long eventoId, Long tipoId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new SolicitudInvalidaException("La cantidad debe ser mayor a 0");
        }

        int filas = tipoEntradaRepository.descontarAforo(tipoId, eventoId, cantidad);
        if (filas == 0) {
            TipoEntrada existente = tipoEntradaRepository.findById(tipoId)
                    .filter(t -> t.getEvento().getId().equals(eventoId))
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Tipo de entrada " + tipoId + " no encontrado en el evento " + eventoId));
            throw new SinAforoException("Aforo insuficiente: quedan " + existente.getAforoDisponible()
                    + " entradas disponibles y se pidieron " + cantidad);
        }

        TipoEntrada actualizado = tipoEntradaRepository.findById(tipoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de entrada " + tipoId + " no encontrado"));
        return aRespuestaTipo(actualizado);
    }

    // ---------- Auxiliares ----------

    private Evento buscar(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento con id " + id + " no encontrado"));
    }

    private void validarEvento(EventoRequest r) {
        if (r == null || r.nombre() == null || r.nombre().isBlank()) {
            throw new SolicitudInvalidaException("El nombre del evento es obligatorio");
        }
        if (r.fechaEvento() == null) {
            throw new SolicitudInvalidaException("La fecha del evento es obligatoria");
        }
    }

    private void validarTipo(TipoEntradaRequest t) {
        if (t == null || t.nombre() == null || t.nombre().isBlank()) {
            throw new SolicitudInvalidaException("Cada tipo de entrada necesita un nombre");
        }
        if (t.precio() == null || t.precio().compareTo(BigDecimal.ZERO) < 0) {
            throw new SolicitudInvalidaException("El precio debe ser mayor o igual a 0");
        }
        if (t.aforo() == null || t.aforo() < 0) {
            throw new SolicitudInvalidaException("El aforo debe ser mayor o igual a 0");
        }
    }

    private void aplicarDatos(Evento evento, EventoRequest r) {
        evento.setNombre(r.nombre());
        evento.setDescripcion(r.descripcion());
        evento.setLugar(r.lugar());
        evento.setFechaEvento(r.fechaEvento());
    }

    private TipoEntrada construirTipo(TipoEntradaRequest t) {
        TipoEntrada tipo = new TipoEntrada();
        tipo.setNombre(t.nombre());
        tipo.setPrecio(t.precio());
        tipo.setAforo(t.aforo());
        tipo.setAforoDisponible(t.aforo());
        return tipo;
    }

    private TipoEntradaResponse aRespuestaTipo(TipoEntrada t) {
        return new TipoEntradaResponse(t.getId(), t.getNombre(), t.getPrecio(), t.getAforo(), t.getAforoDisponible());
    }

    private EventoResponse aRespuesta(Evento e) {
        List<TipoEntradaResponse> tipos = e.getTiposEntrada().stream().map(this::aRespuestaTipo).toList();
        return new EventoResponse(e.getId(), e.getNombre(), e.getDescripcion(), e.getLugar(),
                e.getFechaEvento(), tipos);
    }
}
