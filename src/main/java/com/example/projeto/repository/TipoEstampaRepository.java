package com.example.projeto.repository;

import com.example.projeto.entity.TipoEstampa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoEstampaRepository extends JpaRepository<TipoEstampa, Long> {
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    List<TipoEstampa> findAllByOrderByNomeAsc();
}
