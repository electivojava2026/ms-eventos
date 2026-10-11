package com.eventpass.ms_eventos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.eventpass.ms_eventos.dto.EventoResponse;
import com.eventpass.ms_eventos.dto.EventoRequest;
import com.eventpass.ms_eventos.dto.TipoEntradaRequest;
import com.eventpass.ms_eventos.security.JwtService;
import com.eventpass.ms_eventos.service.EventoService;

/** Verifica que cada rol (USER / STAFF) acceda solo a los endpoints que le corresponden. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventoSeguridadTest {

    private static final String BODY = """
            {"nombre":"Evento Test","fechaEvento":"2026-12-15T20:00:00",
             "tiposEntrada":[{"nombre":"General","precio":1000,"aforo":10}]}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EventoService eventoService;

    private String bearer(String role) {
        return "Bearer " + jwtService.generateToken(role.toLowerCase() + "@eventpass.com", role);
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/eventos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/eventos").header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void compradorPuedeConsultarEventos() throws Exception {
        mockMvc.perform(get("/api/v1/eventos").header("Authorization", bearer("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void compradorNoPuedeCrearEventos() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                .header("Authorization", bearer("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void staffPuedeCrearEventos() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                .header("Authorization", bearer("STAFF"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Evento Test"))
                .andExpect(jsonPath("$.tiposEntrada[0].aforoDisponible").value(10));
    }

    private String urlReserva() {
        EventoResponse ev = eventoService.crear(new EventoRequest("Evento Reserva", null, null,
                LocalDateTime.of(2026, 12, 15, 20, 0), List.of(new TipoEntradaRequest("General", BigDecimal.TEN, 10))));
        return "/api/v1/eventos/" + ev.id() + "/tipos-entrada/" + ev.tiposEntrada().get(0).id() + "/reservas";
    }

    @Test
    void cuentaDeServicioPuedeReservarAforo() throws Exception {
        mockMvc.perform(post(urlReserva())
                .header("Authorization", bearer("SERVICE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cantidad\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aforoDisponible").value(8));
    }

    @Test
    void compradorNoPuedeReservarAforo() throws Exception {
        mockMvc.perform(post(urlReserva())
                .header("Authorization", bearer("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cantidad\":2}"))
                .andExpect(status().isForbidden());
    }
}
