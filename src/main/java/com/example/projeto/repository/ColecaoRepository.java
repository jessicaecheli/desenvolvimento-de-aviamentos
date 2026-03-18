package com.example.projeto.repository;

import com.example.projeto.entity.Colecao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColecaoRepository extends JpaRepository<Colecao, Long> {
    boolean existsByNomeIgnoreCaseAndAnoAndIdNot(String nome, Integer ano, Long id);
}
