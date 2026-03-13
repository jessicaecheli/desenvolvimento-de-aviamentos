package com.example.projeto.repository;

import com.example.projeto.entity.Desenvolvimento;
import com.example.projeto.entity.StatusDesenvolvimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DesenvolvimentoRepository extends JpaRepository<Desenvolvimento, Long> {

    List<Desenvolvimento> findByColecaoId(Long colecaoId);

    List<Desenvolvimento> findByMarcaId(Long marcaId);

    List<Desenvolvimento> findByCategoriaId(Long categoriaId);

    List<Desenvolvimento> findByStatus(StatusDesenvolvimento status);

    @Query("SELECT d FROM Desenvolvimento d WHERE " +
           "(:colecaoId IS NULL OR d.colecao.id = :colecaoId) AND " +
           "(:marcaId IS NULL OR d.marca.id = :marcaId) AND " +
           "(:categoriaId IS NULL OR d.categoria.id = :categoriaId) AND " +
           "(:status IS NULL OR d.status = :status)")
    List<Desenvolvimento> findWithFilters(
        @Param("colecaoId") Long colecaoId,
        @Param("marcaId") Long marcaId,
        @Param("categoriaId") Long categoriaId,
        @Param("status") StatusDesenvolvimento status
    );

    List<Desenvolvimento> findByStatusIn(List<StatusDesenvolvimento> statuses);
}
