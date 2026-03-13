package com.example.projeto.controller;

import com.example.projeto.entity.Colecao;
import com.example.projeto.service.ColecaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/colecoes")
public class ColecaoController {

    private final ColecaoService service;

    public ColecaoController(ColecaoService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("colecoes", service.listarTodas());
        return "cadastros/colecoes/lista";
    }

    @GetMapping("/nova")
    public String novaForm(Model model) {
        model.addAttribute("colecao", new Colecao());
        return "cadastros/colecoes/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("colecao", service.buscarPorId(id));
        return "cadastros/colecoes/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Colecao colecao, RedirectAttributes ra) {
        service.salvar(colecao);
        ra.addFlashAttribute("sucesso", "Coleção salva com sucesso.");
        return "redirect:/colecoes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Coleção excluída.");
        return "redirect:/colecoes";
    }
}
