package com.eventpass.ms_eventos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eventpass.ms_eventos.model.TipoEntrada;

@Repository
public interface TipoEntradaRepository extends JpaRepository<TipoEntrada, Long> {

    /**
     * Descuenta aforo de forma atomica: la condicion "aforoDisponible >= cantidad" va dentro
     * del mismo UPDATE, asi dos compras simultaneas nunca pueden dejar el aforo en negativo.
     * Devuelve las filas modificadas (1 = reservado, 0 = no habia cupo o no existe).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update TipoEntrada t set t.aforoDisponible = t.aforoDisponible - :cantidad "
            + "where t.id = :id and t.evento.id = :eventoId and t.aforoDisponible >= :cantidad")
    int descontarAforo(@Param("id") Long id, @Param("eventoId") Long eventoId, @Param("cantidad") int cantidad);
}
