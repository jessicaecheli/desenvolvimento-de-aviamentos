package com.example.projeto.controller;

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

    public DashboardController(DashboardService service, ColecaoService colecaoService) {
        this.service = service;
        this.colecaoService = colecaoService;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) Long colecaoId, Model model) {
        model.addAttribute("dashboard", service.gerarDashboard(colecaoId));
        model.addAttribute("colecoes", colecaoService.listarTodas());
        model.addAttribute("filtroColecaoId", colecaoId);
        return "dashboard";
    }
}
