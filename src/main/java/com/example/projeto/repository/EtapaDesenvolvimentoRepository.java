package com.example.projeto.repository;

import com.example.projeto.entity.EtapaDesenvolvimento;
import com.example.projeto.entity.TipoEtapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EtapaDesenvolvimentoRepository extends JpaRepository<EtapaDesenvolvimento, Long> {

    List<EtapaDesenvolvimento> findByDesenvolvimentoIdOrderByDataOcorrenciaAscIdAsc(Long desenvolvimentoId);

    Optional<EtapaDesenvolvimento> findFirstByDesenvolvimentoIdAndTipoOrderByDataOcorrenciaAsc(Long desenvolvimentoId, TipoEtapa tipo);

    @Query("SELECT COUNT(DISTINCT e.desenvolvimento.id) FROM EtapaDesenvolvimento e WHERE e.tipo = :tipo AND e.desenvolvimento.id IN :ids")
    long countDistinctByTipoAndDesenvolvimentoIds(@Param("tipo") TipoEtapa tipo, @Param("ids") Collection<Long> ids);
}
