package com.example.projeto.repository;

import com.example.projeto.entity.DadosTecido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DadosTecidoRepository extends JpaRepository<DadosTecido, Long> {
    Optional<DadosTecido> findByDesenvolvimentoId(Long desenvolvimentoId);
}
