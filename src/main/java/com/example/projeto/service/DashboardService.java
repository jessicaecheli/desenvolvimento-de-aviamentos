package com.example.projeto.service;

import com.example.projeto.dto.DashboardDTO;
import com.example.projeto.entity.*;
import com.example.projeto.repository.DesenvolvimentoRepository;
import com.example.projeto.repository.EtapaDesenvolvimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DesenvolvimentoRepository desenvolvimentoRepository;
    private final EtapaDesenvolvimentoRepository etapaRepository;
    private final DesenvolvimentoService desenvolvimentoService;

    public DashboardService(DesenvolvimentoRepository desenvolvimentoRepository,
                             EtapaDesenvolvimentoRepository etapaRepository,
                             DesenvolvimentoService desenvolvimentoService) {
        this.desenvolvimentoRepository = desenvolvimentoRepository;
        this.etapaRepository = etapaRepository;
        this.desenvolvimentoService = desenvolvimentoService;
    }

    public DashboardDTO gerarDashboard() {
        return gerarDashboard(null, null);
    }

    public DashboardDTO gerarDashboard(Long colecaoId) {
        return gerarDashboard(colecaoId, null);
    }

    @Transactional(readOnly = true)
    public DashboardDTO gerarDashboard(Long colecaoId, Long categoriaMasterId) {
        List<Desenvolvimento> todos = colecaoId != null
            ? desenvolvimentoRepository.findByColecaoId(colecaoId)
            : desenvolvimentoRepository.findAll();

        if (categoriaMasterId != null) {
            todos = todos.stream()
                .filter(d -> d.getCategoria() != null
                    && d.getCategoria().getCategoriaPai() != null
                    && d.getCategoria().getCategoriaPai().getId().equals(categoriaMasterId))
                .collect(Collectors.toList());
        }
        DashboardDTO dto = new DashboardDTO();

        // Total por coleção
        Map<String, Long> porColecao = todos.stream()
            .filter(d -> d.getColecao() != null)
            .collect(Collectors.groupingBy(
                d -> d.getColecao().getNomeCompleto(),
                Collectors.counting()
            ));
        dto.setTotalPorColecao(porColecao);

        // Total por status
        Map<StatusDesenvolvimento, Long> porStatus = todos.stream()
            .collect(Collectors.groupingBy(Desenvolvimento::getStatus, Collectors.counting()));
        dto.setTotalPorStatus(porStatus);

        dto.setTotalAprovados(porStatus.getOrDefault(StatusDesenvolvimento.APROVADO, 0L));
        dto.setTotalCancelados(porStatus.getOrDefault(StatusDesenvolvimento.CANCELADO, 0L));
        List<Long> ids = todos.stream().map(Desenvolvimento::getId).collect(Collectors.toList());
        dto.setTotalComAlteracao(ids.isEmpty() ? 0L
            : etapaRepository.countDistinctByTipoAndDesenvolvimentoIds(TipoEtapa.ALTERACAO, ids));

        // Atrasados e com entrega na semana
        List<DashboardDTO.AtrasadoDTO> atrasados = todos.stream()
            .filter(d -> desenvolvimentoService.estaAtrasado(d) || desenvolvimentoService.venceEstaSemana(d))
            .map(d -> {
                boolean late = desenvolvimentoService.estaAtrasado(d);
                long dias = late ? desenvolvimentoService.diasAtraso(d) : desenvolvimentoService.diasRestantes(d);
                return new DashboardDTO.AtrasadoDTO(
                    d.getId(),
                    d.getCodigo(),
                    d.getDescricao(),
                    d.getColecao() != null ? d.getColecao().getNomeCompleto() : "-",
                    d.getCategoria() != null ? d.getCategoria().getNome() : "-",
                    dias,
                    late
                );
            })
            .sorted(Comparator.comparingInt((DashboardDTO.AtrasadoDTO a) -> a.isAtrasado() ? 0 : 1)
                .thenComparingLong(a -> a.isAtrasado() ? -a.getDiasAtraso() : a.getDiasAtraso()))
            .collect(Collectors.toList());
        dto.setAtrasados(atrasados);

        // Leadtime médio por categoria (dias úteis da primeira à última etapa do desenvolvimento)
        Map<String, List<Long>> leadtimesPorCategoria = new LinkedHashMap<>();
        for (Desenvolvimento dev : todos) {
            if (dev.getCategoria() == null) continue;
            List<EtapaDesenvolvimento> etapas = etapaRepository
                .findByDesenvolvimentoIdOrderByDataOcorrenciaAscIdAsc(dev.getId());
            if (etapas.size() < 2) continue;
            long dias = desenvolvimentoService.calcularDiasUteis(
                etapas.get(0).getDataOcorrencia(),
                etapas.get(etapas.size() - 1).getDataOcorrencia()
            );
            leadtimesPorCategoria
                .computeIfAbsent(dev.getCategoria().getNome(), k -> new ArrayList<>())
                .add(dias);
        }
        Map<String, Double> medias = new LinkedHashMap<>();
        leadtimesPorCategoria.forEach((k, v) -> medias.put(k, v.stream().mapToLong(Long::longValue).average().orElse(0)));
        dto.setLeadtimeMediaPorCategoria(medias);

        // Histórico de atrasados (todos que já ultrapassaram o prazo na fase de amostra)
        Set<TipoEtapa> faseAmostra = Set.of(TipoEtapa.AMOSTRA, TipoEtapa.ALTERACAO);
        Set<StatusDesenvolvimento> statusFaseAmostra = Set.of(StatusDesenvolvimento.AMOSTRA, StatusDesenvolvimento.ALTERACAO);

        long historicoAtrasados = 0;
        for (Desenvolvimento dev : todos) {
            if (dev.getCategoria() == null) continue;
            List<EtapaDesenvolvimento> etapas = etapaRepository
                .findByDesenvolvimentoIdOrderByDataOcorrenciaAscIdAsc(dev.getId());

            Optional<EtapaDesenvolvimento> primeiraAmostra = etapas.stream()
                .filter(e -> e.getTipo() == TipoEtapa.AMOSTRA)
                .findFirst();
            if (primeiraAmostra.isEmpty()) continue;

            LocalDate inicio = primeiraAmostra.get().getDataOcorrencia();

            LocalDate fim;
            if (statusFaseAmostra.contains(dev.getStatus())) {
                fim = LocalDate.now();
            } else {
                fim = etapas.stream()
                    .filter(e -> e.getDataOcorrencia().isAfter(inicio) && !faseAmostra.contains(e.getTipo()))
                    .map(EtapaDesenvolvimento::getDataOcorrencia)
                    .findFirst()
                    .orElse(LocalDate.now());
            }

            long diasUteis = desenvolvimentoService.calcularDiasUteis(inicio, fim);
            if (diasUteis > dev.getCategoria().getPrazoDiasUteis()) {
                historicoAtrasados++;
            }
        }
        dto.setTotalHistoricoAtrasados(historicoAtrasados);

        return dto;
    }
}
