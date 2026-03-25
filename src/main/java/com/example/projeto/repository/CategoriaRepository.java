package com.example.projeto.repository;

import com.example.projeto.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    boolean existsByNomeIgnoreCase(String nome);
    List<Categoria> findByCategoriaPaiIsNull();
    List<Categoria> findByCategoriaPaiIsNotNull();
    List<Categoria> findByCategoriaPaiIsNotNullOrderByCategoriaPaiNomeAscNomeAsc();
    List<Categoria> findByCategoriaPaiId(Long categoriaPaiId);
    List<Categoria> findByCategoriaPaiIdOrderByNomeAsc(Long categoriaPaiId);
}
