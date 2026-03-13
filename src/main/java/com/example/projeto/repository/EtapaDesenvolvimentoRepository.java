package com.example.projeto.repository;

import com.example.projeto.entity.EtapaDesenvolvimento;
import com.example.projeto.entity.TipoEtapa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtapaDesenvolvimentoRepository extends JpaRepository<EtapaDesenvolvimento, Long> {

    List<EtapaDesenvolvimento> findByDesenvolvimentoIdOrderByDataOcorrenciaAscIdAsc(Long desenvolvimentoId);

    Optional<EtapaDesenvolvimento> findFirstByDesenvolvimentoIdAndTipoOrderByDataOcorrenciaAsc(Long desenvolvimentoId, TipoEtapa tipo);
}
