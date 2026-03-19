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

    @Query("SELECT DISTINCT d FROM Desenvolvimento d " +
           "LEFT JOIN d.etapas e LEFT JOIN e.orcamento o WHERE " +
           "(:colecaoId IS NULL OR d.colecao.id = :colecaoId) AND " +
           "(:marcaId IS NULL OR d.marca.id = :marcaId) AND " +
           "(:categoriaId IS NULL OR d.categoria.id = :categoriaId) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:codigo IS NULL OR UPPER(d.codigo) LIKE UPPER(CONCAT('%', :codigo, '%'))) AND " +
           "(:fornecedor IS NULL OR UPPER(o.fornecedor) LIKE UPPER(CONCAT('%', :fornecedor, '%'))) " +
           "ORDER BY d.id DESC")
    List<Desenvolvimento> findWithFilters(
        @Param("colecaoId") Long colecaoId,
        @Param("marcaId") Long marcaId,
        @Param("categoriaId") Long categoriaId,
        @Param("status") StatusDesenvolvimento status,
        @Param("codigo") String codigo,
        @Param("fornecedor") String fornecedor
    );

    List<Desenvolvimento> findByStatusIn(List<StatusDesenvolvimento> statuses);
}
