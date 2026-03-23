package com.example.projeto.repository;

import com.example.projeto.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
