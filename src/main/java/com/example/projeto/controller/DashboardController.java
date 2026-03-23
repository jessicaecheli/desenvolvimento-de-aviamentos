package com.example.projeto.controller;

import com.example.projeto.service.CategoriaService;
import com.example.projeto.service.ColecaoService;
import com.example.projeto.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class DashboardController {

    private final DashboardService service;
    private final ColecaoService colecaoService;
    private final CategoriaService categoriaService;

    public DashboardController(DashboardService service, ColecaoService colecaoService,
                                CategoriaService categoriaService) {
        this.service = service;
        this.colecaoService = colecaoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) Long colecaoId,
                             @RequestParam(required = false) Long categoriaMasterId,
                             Model model) {
        model.addAttribute("dashboard", service.gerarDashboard(colecaoId, categoriaMasterId));
        model.addAttribute("colecoes", colecaoService.listarTodas());
        model.addAttribute("categoriasMaster", categoriaService.listarMasters());
        model.addAttribute("filtroColecaoId", colecaoId);
        model.addAttribute("filtroCategoriaMasterId", categoriaMasterId);
        return "dashboard";
    }
}
