package com.example.projeto.controller;

import com.example.projeto.entity.DadosTecido;
import com.example.projeto.repository.DadosTecidoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final DadosTecidoRepository dadosTecidoRepository;

    public ReservaController(DadosTecidoRepository dadosTecidoRepository) {
        this.dadosTecidoRepository = dadosTecidoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        List<DadosTecido> reservas = dadosTecidoRepository.findAllComReserva();
        LocalDate hoje = LocalDate.now();
        long expiradas = reservas.stream()
            .filter(r -> r.getDataTermino() != null && r.getDataTermino().isBefore(hoje))
            .count();
        long venceHoje = reservas.stream()
            .filter(r -> r.getDataTermino() != null && r.getDataTermino().isEqual(hoje))
            .count();
        model.addAttribute("reservas", reservas);
        model.addAttribute("hoje", hoje);
        model.addAttribute("expiradas", expiradas);
        model.addAttribute("venceHoje", venceHoje);
        return "reservas/lista";
    }
}
