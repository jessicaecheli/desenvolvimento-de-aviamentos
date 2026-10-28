package com.example.projeto.repository;

import com.example.projeto.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    List<Orcamento> findByDesenvolvimentoIdOrderByIdAsc(Long desenvolvimentoId);

    /** Orçamentos ativos (não excluídos) do desenvolvimento. */
    @Query("SELECT o FROM Orcamento o WHERE o.desenvolvimento.id = :devId " +
           "AND (o.excluido IS NULL OR o.excluido = false) ORDER BY o.id")
    List<Orcamento> findAtivosByDesenvolvimentoId(@Param("devId") Long desenvolvimentoId);

    @Query("SELECT COUNT(o) FROM Orcamento o WHERE o.desenvolvimento.id = :devId " +
           "AND (o.excluido IS NULL OR o.excluido = false)")
    long countAtivosByDesenvolvimentoId(@Param("devId") Long desenvolvimentoId);
}
