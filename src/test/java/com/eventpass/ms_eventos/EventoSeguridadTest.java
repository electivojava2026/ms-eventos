package com.eventpass.ms_eventos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.eventpass.ms_eventos.security.JwtService;

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
}
