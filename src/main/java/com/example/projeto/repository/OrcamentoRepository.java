package com.example.projeto.repository;

import com.example.projeto.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    List<Orcamento> findByDesenvolvimentoIdOrderByNumeroAsc(Long desenvolvimentoId);
}
