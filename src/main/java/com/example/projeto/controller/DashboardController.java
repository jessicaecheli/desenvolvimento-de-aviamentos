package com.example.projeto.controller;

import com.example.projeto.entity.DadosTecido;
import com.example.projeto.repository.DadosTecidoRepository;
import com.example.projeto.service.CategoriaService;
import com.example.projeto.service.ColecaoService;
import com.example.projeto.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/")
public class DashboardController {

    private final DashboardService service;
    private final ColecaoService colecaoService;
    private final CategoriaService categoriaService;
    private final DadosTecidoRepository dadosTecidoRepository;

    public DashboardController(DashboardService service, ColecaoService colecaoService,
                                CategoriaService categoriaService,
                                DadosTecidoRepository dadosTecidoRepository) {
        this.service = service;
        this.colecaoService = colecaoService;
        this.categoriaService = categoriaService;
        this.dadosTecidoRepository = dadosTecidoRepository;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) Long colecaoId,
                             @RequestParam(required = false) Long categoriaMasterId,
                             @RequestParam(required = false) String limpar,
                             Model model) {
        var colecoes = colecaoService.listarTodas();

        // Se nenhum filtro informado e não está limpando, usa a última coleção cadastrada
        if (colecaoId == null && limpar == null && !colecoes.isEmpty()) {
            colecaoId = colecoes.stream()
                .mapToLong(c -> c.getId())
                .max()
                .getAsLong();
        }

        model.addAttribute("dashboard", service.gerarDashboard(colecaoId, categoriaMasterId));
        model.addAttribute("colecoes", colecoes);
        model.addAttribute("categoriasMaster", categoriaService.listarMasters());
        model.addAttribute("filtroColecaoId", colecaoId);
        model.addAttribute("filtroCategoriaMasterId", categoriaMasterId);

        LocalDate hoje = LocalDate.now();
        List<DadosTecido> reservas = dadosTecidoRepository.findAllComReserva();
        long reservasExpiradas = reservas.stream()
            .filter(r -> r.getDataTermino() != null && r.getDataTermino().isBefore(hoje))
            .count();
        long reservasVenceHoje = reservas.stream()
            .filter(r -> r.getDataTermino() != null && r.getDataTermino().isEqual(hoje))
            .count();
        model.addAttribute("reservasExpiradas", reservasExpiradas);
        model.addAttribute("reservasVenceHoje", reservasVenceHoje);
        model.addAttribute("totalReservas", reservas.size());
        return "dashboard";
    }
}
