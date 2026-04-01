package com.example.projeto.controller;

import com.example.projeto.dto.DashboardDTO;
import com.example.projeto.entity.Colecao;
import com.example.projeto.service.ColecaoService;
import com.example.projeto.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class ComparativoController {

    private final DashboardService dashboardService;
    private final ColecaoService colecaoService;

    public ComparativoController(DashboardService dashboardService, ColecaoService colecaoService) {
        this.dashboardService = dashboardService;
        this.colecaoService = colecaoService;
    }

    @GetMapping("/comparativos")
    public String comparativos(
            @RequestParam(required = false) Long col1,
            @RequestParam(required = false) Long col2,
            @RequestParam(required = false) Long col3,
            @RequestParam(required = false) Long col4,
            Model model) {

        List<Colecao> todas = colecaoService.listarTodas();
        model.addAttribute("todasColecoes", todas);
        model.addAttribute("col1", col1);
        model.addAttribute("col2", col2);
        model.addAttribute("col3", col3);
        model.addAttribute("col4", col4);

        List<Long> colIds = Stream.of(col1, col2, col3, col4)
                .filter(Objects::nonNull).collect(Collectors.toList());

        if (colIds.isEmpty()) {
            model.addAttribute("temDados", false);
            return "comparativos";
        }

        Map<Long, String> nomesMap = todas.stream()
                .collect(Collectors.toMap(Colecao::getId, Colecao::getNomeCompleto));

        List<String> colNomes = colIds.stream()
                .map(id -> nomesMap.getOrDefault(id, "Coleção " + id))
                .collect(Collectors.toList());

        List<DashboardDTO> dashboards = colIds.stream()
                .map(dashboardService::gerarDashboard)
                .collect(Collectors.toList());

        model.addAttribute("colNomes", colNomes);
        model.addAttribute("aprovadosData",
                dashboards.stream().map(DashboardDTO::getTotalAprovados).collect(Collectors.toList()));
        model.addAttribute("canceladosData",
                dashboards.stream().map(DashboardDTO::getTotalCancelados).collect(Collectors.toList()));
        model.addAttribute("alteracoesData",
                dashboards.stream().map(DashboardDTO::getTotalComAlteracao).collect(Collectors.toList()));
        model.addAttribute("atrasosData",
                dashboards.stream().map(DashboardDTO::getTotalHistoricoAtrasados).collect(Collectors.toList()));
        model.addAttribute("custoAmostrasData",
                dashboards.stream().map(d -> d.getTotalCustoAmostras() != null
                        ? d.getTotalCustoAmostras().doubleValue() : 0.0).collect(Collectors.toList()));
        model.addAttribute("descontoMedioData",
                dashboards.stream().map(d -> d.getPercentualDescontoMedio() != null
                        ? d.getPercentualDescontoMedio() : 0.0).collect(Collectors.toList()));
        model.addAttribute("totalDesenvData",
                dashboards.stream().map(d -> d.getTotalPorStatus().values().stream()
                        .mapToLong(Long::longValue).sum()).collect(Collectors.toList()));

        // Agrupado: Marca
        List<String> marcasLabels = dashboards.stream()
                .flatMap(d -> d.getTotalPorMarca() != null
                        ? d.getTotalPorMarca().keySet().stream() : Stream.empty())
                .distinct().sorted().collect(Collectors.toList());
        List<Map<String, Object>> marcasDatasets = new ArrayList<>();
        for (int i = 0; i < dashboards.size(); i++) {
            Map<String, Long> porMarca = dashboards.get(i).getTotalPorMarca();
            List<Long> vals = marcasLabels.stream()
                    .map(m -> porMarca != null ? porMarca.getOrDefault(m, 0L) : 0L)
                    .collect(Collectors.toList());
            Map<String, Object> ds = new LinkedHashMap<>();
            ds.put("label", colNomes.get(i));
            ds.put("data", vals);
            marcasDatasets.add(ds);
        }
        model.addAttribute("marcasLabels", marcasLabels);
        model.addAttribute("marcasDatasets", marcasDatasets);

        // Agrupado: Subcategoria
        List<String> subcatLabels = dashboards.stream()
                .flatMap(d -> d.getTotalPorSubcategoria() != null
                        ? d.getTotalPorSubcategoria().keySet().stream() : Stream.empty())
                .distinct().sorted().collect(Collectors.toList());
        List<Map<String, Object>> subcatDatasets = new ArrayList<>();
        for (int i = 0; i < dashboards.size(); i++) {
            Map<String, Long> porSubcat = dashboards.get(i).getTotalPorSubcategoria();
            List<Long> vals = subcatLabels.stream()
                    .map(s -> porSubcat != null ? porSubcat.getOrDefault(s, 0L) : 0L)
                    .collect(Collectors.toList());
            Map<String, Object> ds = new LinkedHashMap<>();
            ds.put("label", colNomes.get(i));
            ds.put("data", vals);
            subcatDatasets.add(ds);
        }
        model.addAttribute("subcatLabels", subcatLabels);
        model.addAttribute("subcatDatasets", subcatDatasets);

        model.addAttribute("temDados", true);
        return "comparativos";
    }
}
