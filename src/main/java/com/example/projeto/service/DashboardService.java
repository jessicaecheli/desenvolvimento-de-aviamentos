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

        // Leadtime médio por subcategoria — 1ª Liberada e última Liberada
        Map<String, List<Long>> leadtimesPrimeiraLib = new LinkedHashMap<>();
        Map<String, List<Long>> leadtimesUltimaLib  = new LinkedHashMap<>();
        for (Desenvolvimento dev : todos) {
            if (dev.getCategoria() == null) continue;
            List<EtapaDesenvolvimento> etapas = etapaRepository
                .findByDesenvolvimentoIdOrderByDataOcorrenciaAscIdAsc(dev.getId());
            Optional<EtapaDesenvolvimento> etapaInicial = etapas.stream().findFirst();
            List<EtapaDesenvolvimento> liberadas = etapas.stream()
                .filter(e -> e.getTipo() == TipoEtapa.LIBERADA)
                .toList();
            if (etapaInicial.isEmpty() || liberadas.isEmpty()) continue;
            String cat = dev.getCategoria().getNome();
            long diasPrimeira = desenvolvimentoService.calcularDiasUteis(
                etapaInicial.get().getDataOcorrencia(),
                liberadas.get(0).getDataOcorrencia()
            );
            long diasUltima = desenvolvimentoService.calcularDiasUteis(
                etapaInicial.get().getDataOcorrencia(),
                liberadas.get(liberadas.size() - 1).getDataOcorrencia()
            );
            leadtimesPrimeiraLib.computeIfAbsent(cat, k -> new ArrayList<>()).add(diasPrimeira);
            leadtimesUltimaLib.computeIfAbsent(cat, k -> new ArrayList<>()).add(diasUltima);
        }
        Map<String, Double> medias = new LinkedHashMap<>();
        leadtimesPrimeiraLib.forEach((k, v) -> medias.put(k, v.stream().mapToLong(Long::longValue).average().orElse(0)));
        dto.setLeadtimeMediaPorCategoria(medias);
        Map<String, Double> mediasFinais = new LinkedHashMap<>();
        leadtimesUltimaLib.forEach((k, v) -> mediasFinais.put(k, v.stream().mapToLong(Long::longValue).average().orElse(0)));
        dto.setLeadtimeMediaFinalPorCategoria(mediasFinais);

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

        // Custo total com amostras
        java.math.BigDecimal custoAmostras = ids.isEmpty()
            ? java.math.BigDecimal.ZERO
            : etapaRepository.sumCustoAmostraByDesenvolvimentoIds(ids);
        dto.setTotalCustoAmostras(custoAmostras);

        // Negociação: total de desconto e % médio
        List<Desenvolvimento> comNegociacao = todos.stream()
            .filter(d -> d.getPrecoInicial() != null && d.getPrecoFinal() != null
                && d.getPrecoInicial().compareTo(java.math.BigDecimal.ZERO) > 0
                && d.getQtdCompraMostruario() != null)
            .toList();
        java.math.BigDecimal totalDesconto = comNegociacao.stream()
            .map(d -> d.getPrecoInicial().subtract(d.getPrecoFinal())
                .multiply(java.math.BigDecimal.valueOf(d.getQtdCompraMostruario())))
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        dto.setTotalDescontoNegociado(totalDesconto);

        OptionalDouble mediaDesconto = comNegociacao.stream()
            .mapToDouble(d -> d.getPrecoInicial().subtract(d.getPrecoFinal())
                .divide(d.getPrecoInicial(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(100))
                .doubleValue())
            .average();
        dto.setPercentualDescontoMedio(mediaDesconto.isPresent() ? mediaDesconto.getAsDouble() : null);

        // Quantidade por marca (inclui todos os status)
        Map<String, Long> porMarca = todos.stream()
            .filter(d -> d.getMarca() != null)
            .collect(Collectors.groupingBy(
                d -> d.getMarca().getNome(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new
            ));
        dto.setTotalPorMarca(porMarca);

        return dto;
    }
}
