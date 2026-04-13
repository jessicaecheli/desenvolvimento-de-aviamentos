package com.example.projeto.repository;

import com.example.projeto.entity.DadosTecido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DadosTecidoRepository extends JpaRepository<DadosTecido, Long> {
    Optional<DadosTecido> findByDesenvolvimentoId(Long desenvolvimentoId);
    List<DadosTecido> findByDesenvolvimentoIdIn(List<Long> ids);

    @Query("SELECT dt FROM DadosTecido dt JOIN FETCH dt.desenvolvimento d WHERE dt.reserva IS NOT NULL AND dt.reserva <> '' AND d.status = com.example.projeto.entity.StatusDesenvolvimento.LIBERADA ORDER BY dt.dataTermino ASC NULLS LAST")
    List<DadosTecido> findAllComReserva();
}
