package com.example.projeto.service;

import com.example.projeto.dto.DashboardDTO;
import com.example.projeto.entity.*;
import com.example.projeto.repository.DesenvolvimentoRepository;
import com.example.projeto.repository.EtapaDesenvolvimentoRepository;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
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
        List<Desenvolvimento> todos = desenvolvimentoRepository.findAll();
        DashboardDTO dto = new DashboardDTO();

        // Total por coleção
        Map<String, Long> porColecao = todos.stream()
            .filter(d -> d.getColecao() != null)
            .collect(Collectors.groupingBy(
                d -> d.getColecao().getNome() + " " + (d.getColecao().getAno() != null ? d.getColecao().getAno() : ""),
                Collectors.counting()
            ));
        dto.setTotalPorColecao(porColecao);

        // Total por status
        Map<StatusDesenvolvimento, Long> porStatus = todos.stream()
            .collect(Collectors.groupingBy(Desenvolvimento::getStatus, Collectors.counting()));
        dto.setTotalPorStatus(porStatus);

        dto.setTotalAprovados(porStatus.getOrDefault(StatusDesenvolvimento.APROVADO, 0L));
        dto.setTotalCancelados(porStatus.getOrDefault(StatusDesenvolvimento.CANCELADO, 0L));
        dto.setTotalComAlteracao(porStatus.getOrDefault(StatusDesenvolvimento.ALTERACAO, 0L));

        // Atrasados
        List<DashboardDTO.AtrasadoDTO> atrasados = todos.stream()
            .filter(desenvolvimentoService::estaAtrasado)
            .map(d -> new DashboardDTO.AtrasadoDTO(
                d.getCodigo(),
                d.getDescricao(),
                d.getColecao() != null ? d.getColecao().getNome() : "-",
                d.getCategoria() != null ? d.getCategoria().getNome() : "-",
                desenvolvimentoService.diasAtraso(d)
            ))
            .sorted(Comparator.comparingLong(DashboardDTO.AtrasadoDTO::getDiasAtraso).reversed())
            .collect(Collectors.toList());
        dto.setAtrasados(atrasados);

        // Leadtime médio por etapa (dias corridos entre etapas consecutivas)
        Map<String, List<Long>> leadtimes = new LinkedHashMap<>();
        for (Desenvolvimento dev : todos) {
            List<EtapaDesenvolvimento> etapas = etapaRepository
                .findByDesenvolvimentoIdOrderByDataOcorrenciaAscIdAsc(dev.getId());
            for (int i = 1; i < etapas.size(); i++) {
                EtapaDesenvolvimento anterior = etapas.get(i - 1);
                EtapaDesenvolvimento atual = etapas.get(i);
                String chave = anterior.getTipo().name() + " → " + atual.getTipo().name();
                long dias = ChronoUnit.DAYS.between(anterior.getDataOcorrencia(), atual.getDataOcorrencia());
                leadtimes.computeIfAbsent(chave, k -> new ArrayList<>()).add(dias);
            }
        }
        Map<String, Double> medias = new LinkedHashMap<>();
        leadtimes.forEach((k, v) -> medias.put(k, v.stream().mapToLong(Long::longValue).average().orElse(0)));
        dto.setLeadtimeMediaPorEtapa(medias);

        return dto;
    }
}
