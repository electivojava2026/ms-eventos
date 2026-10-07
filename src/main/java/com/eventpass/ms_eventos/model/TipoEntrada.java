package com.eventpass.ms_eventos.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipos_entrada")
public class TipoEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    // Capacidad total de este tipo de entrada
    @Column(nullable = false)
    private Integer aforo;

    // Cupos que quedan; baja cuando se reserva aforo (evita la sobreventa)
    @Column(name = "aforo_disponible", nullable = false)
    private Integer aforoDisponible;

    // Muchos tipos de entrada pertenecen a un evento
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getAforo() { return aforo; }
    public void setAforo(Integer aforo) { this.aforo = aforo; }

    public Integer getAforoDisponible() { return aforoDisponible; }
    public void setAforoDisponible(Integer aforoDisponible) { this.aforoDisponible = aforoDisponible; }

    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }
}
