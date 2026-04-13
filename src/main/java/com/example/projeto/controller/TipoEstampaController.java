package com.example.projeto.controller;

import com.example.projeto.entity.TipoEstampa;
import com.example.projeto.service.TipoEstampaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tipos-estampa")
public class TipoEstampaController {

    private final TipoEstampaService service;

    public TipoEstampaController(TipoEstampaService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("tiposEstampa", service.listarTodos());
        return "cadastros/tipos-estampa/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("tipoEstampa", new TipoEstampa());
        return "cadastros/tipos-estampa/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("tipoEstampa", service.buscarPorId(id));
        return "cadastros/tipos-estampa/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute TipoEstampa tipoEstampa, RedirectAttributes ra) {
        try {
            service.salvar(tipoEstampa);
            ra.addFlashAttribute("sucesso", "Tipo de tecido salvo com sucesso.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return tipoEstampa.getId() == null
                    ? "redirect:/tipos-estampa/novo"
                    : "redirect:/tipos-estampa/" + tipoEstampa.getId() + "/editar";
        }
        return "redirect:/tipos-estampa";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Tipo de tecido excluído.");
        return "redirect:/tipos-estampa";
    }
}
