package com.example.projeto.controller;

import com.example.projeto.entity.Fornecedor;
import com.example.projeto.service.FornecedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fornecedores")
public class FornecedorController {

    private final FornecedorService service;

    public FornecedorController(FornecedorService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("fornecedores", service.listarTodos());
        return "cadastros/fornecedores/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return "cadastros/fornecedores/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("fornecedor", service.buscarPorId(id));
        return "cadastros/fornecedores/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Fornecedor fornecedor, RedirectAttributes ra) {
        try {
            service.salvar(fornecedor);
            ra.addFlashAttribute("sucesso", "Fornecedor salvo com sucesso.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return fornecedor.getId() == null
                    ? "redirect:/fornecedores/novo"
                    : "redirect:/fornecedores/" + fornecedor.getId() + "/editar";
        }
        return "redirect:/fornecedores";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Fornecedor excluído.");
        return "redirect:/fornecedores";
    }
}
