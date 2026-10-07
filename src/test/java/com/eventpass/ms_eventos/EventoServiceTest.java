package com.eventpass.ms_eventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.eventpass.ms_eventos.dto.EventoRequest;
import com.eventpass.ms_eventos.dto.EventoResponse;
import com.eventpass.ms_eventos.dto.TipoEntradaRequest;
import com.eventpass.ms_eventos.dto.TipoEntradaResponse;
import com.eventpass.ms_eventos.exception.RecursoNoEncontradoException;
import com.eventpass.ms_eventos.exception.SinAforoException;
import com.eventpass.ms_eventos.exception.SolicitudInvalidaException;
import com.eventpass.ms_eventos.service.EventoService;

@SpringBootTest
@Transactional
class EventoServiceTest {

    @Autowired
    private EventoService eventoService;

    private EventoRequest ejemplo() {
        return new EventoRequest("Concierto Rock", "Show en vivo", "Estadio Nacional",
                LocalDateTime.of(2026, 12, 15, 20, 0),
                List.of(new TipoEntradaRequest("General", new BigDecimal("25000"), 500),
                        new TipoEntradaRequest("VIP", new BigDecimal("60000"), 100)));
    }

    private TipoEntradaResponse tipo(EventoResponse evento, String nombre) {
        return evento.tiposEntrada().stream().filter(t -> t.nombre().equals(nombre)).findFirst().orElseThrow();
    }

    @Test
    void creaYObtieneEventoConTiposDeEntrada() {
        EventoResponse creado = eventoService.crear(ejemplo());

        assertNotNull(creado.id());
        EventoResponse leido = eventoService.obtener(creado.id());
        assertEquals("Concierto Rock", leido.nombre());
        assertEquals(2, leido.tiposEntrada().size());
        // al crear, el aforo disponible es igual al aforo total
        assertEquals(500, tipo(leido, "General").aforoDisponible());
    }

    @Test
    void actualizaYEliminaEvento() {
        EventoResponse creado = eventoService.crear(ejemplo());

        EventoRequest cambios = new EventoRequest("Concierto Rock 2", null, "Movistar Arena",
                LocalDateTime.of(2026, 12, 20, 21, 0), null);
        EventoResponse actualizado = eventoService.actualizar(creado.id(), cambios);
        assertEquals("Concierto Rock 2", actualizado.nombre());
        assertEquals(2, actualizado.tiposEntrada().size());

        eventoService.eliminar(creado.id());
        assertThrows(RecursoNoEncontradoException.class, () -> eventoService.obtener(creado.id()));
    }

    @Test
    void agregaTipoDeEntradaAUnEventoExistente() {
        EventoResponse creado = eventoService.crear(ejemplo());

        TipoEntradaResponse nuevo = eventoService.agregarTipoEntrada(creado.id(),
                new TipoEntradaRequest("Palco", new BigDecimal("90000"), 20));

        assertNotNull(nuevo.id());
        assertEquals(20, nuevo.aforoDisponible());
        assertEquals(3, eventoService.obtener(creado.id()).tiposEntrada().size());
    }

    @Test
    void reservaDescuentaElAforoDisponible() {
        EventoResponse evento = eventoService.crear(ejemplo());
        Long general = tipo(evento, "General").id();

        TipoEntradaResponse resultado = eventoService.reservarAforo(evento.id(), general, 3);

        assertEquals(497, resultado.aforoDisponible());
        assertEquals(500, resultado.aforo());
    }

    @Test
    void noPermiteReservarMasEntradasQueElAforoDisponible() {
        EventoResponse evento = eventoService.crear(ejemplo());
        Long vip = tipo(evento, "VIP").id();

        TipoEntradaResponse agotado = eventoService.reservarAforo(evento.id(), vip, 100);
        assertEquals(0, agotado.aforoDisponible());

        assertThrows(SinAforoException.class, () -> eventoService.reservarAforo(evento.id(), vip, 1));
    }

    @Test
    void rechazaCantidadInvalidaYTipoInexistente() {
        EventoResponse evento = eventoService.crear(ejemplo());
        Long general = tipo(evento, "General").id();

        assertThrows(SolicitudInvalidaException.class, () -> eventoService.reservarAforo(evento.id(), general, 0));
        assertThrows(RecursoNoEncontradoException.class, () -> eventoService.reservarAforo(evento.id(), 9999L, 1));
    }

    @Test
    void rechazaEventoSinNombre() {
        EventoRequest invalido = new EventoRequest(" ", null, null, LocalDateTime.now(), null);
        assertThrows(SolicitudInvalidaException.class, () -> eventoService.crear(invalido));
    }

    @Test
    void obtenerInexistenteLanzaExcepcion() {
        assertThrows(RecursoNoEncontradoException.class, () -> eventoService.obtener(9999L));
    }
}
