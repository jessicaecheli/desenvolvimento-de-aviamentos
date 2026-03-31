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
           "(:categoriaMasterId IS NULL OR (d.categoria IS NOT NULL AND d.categoria.categoriaPai.id = :categoriaMasterId)) AND " +
           "(:categoriaId IS NULL OR d.categoria.id = :categoriaId) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:codigo IS NULL OR UPPER(d.codigo) LIKE UPPER(CONCAT('%', :codigo, '%')) " +
           "    OR UPPER(d.codigoSystextil1) LIKE UPPER(CONCAT('%', :codigo, '%')) " +
           "    OR UPPER(d.codigoSystextil2) LIKE UPPER(CONCAT('%', :codigo, '%')) " +
           "    OR UPPER(d.codigoSystextil3) LIKE UPPER(CONCAT('%', :codigo, '%'))) AND " +
           "(:fornecedor IS NULL OR UPPER(o.fornecedor) LIKE UPPER(CONCAT('%', :fornecedor, '%'))) " +
           "ORDER BY d.id DESC")
    List<Desenvolvimento> findWithFilters(
        @Param("colecaoId") Long colecaoId,
        @Param("marcaId") Long marcaId,
        @Param("categoriaMasterId") Long categoriaMasterId,
        @Param("categoriaId") Long categoriaId,
        @Param("status") StatusDesenvolvimento status,
        @Param("codigo") String codigo,
        @Param("fornecedor") String fornecedor
    );

    List<Desenvolvimento> findByStatusIn(List<StatusDesenvolvimento> statuses);

    @Query("SELECT d FROM Desenvolvimento d LEFT JOIN FETCH d.categoria WHERE d.id = :id")
    java.util.Optional<Desenvolvimento> findByIdFetchingCategoria(@Param("id") Long id);

    @Query("SELECT d FROM Desenvolvimento d WHERE d.id <> :excludeId AND (" +
           "(:codigo IS NOT NULL AND (" +
           "  UPPER(d.codigoSystextil1) = UPPER(:codigo) OR " +
           "  UPPER(d.codigoSystextil2) = UPPER(:codigo) OR " +
           "  UPPER(d.codigoSystextil3) = UPPER(:codigo)))" +
           ")")
    List<Desenvolvimento> findBySystextilDuplicado(@Param("codigo") String codigo, @Param("excludeId") Long excludeId);
}
