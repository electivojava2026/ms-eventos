package com.eventpass.ms_eventos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    private String lugar;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    // Un evento tiene muchos tipos de entrada (General, VIP, etc.)
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TipoEntrada> tiposEntrada = new ArrayList<>();

    public void agregarTipoEntrada(TipoEntrada tipo) {
        tiposEntrada.add(tipo);
        tipo.setEvento(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }

    public List<TipoEntrada> getTiposEntrada() { return tiposEntrada; }
    public void setTiposEntrada(List<TipoEntrada> tiposEntrada) { this.tiposEntrada = tiposEntrada; }
}
