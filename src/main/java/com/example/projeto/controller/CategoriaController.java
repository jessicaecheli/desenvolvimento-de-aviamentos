package com.example.projeto.controller;

import com.example.projeto.entity.Categoria;
import com.example.projeto.service.CategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("categorias", service.listarTodas());
        return "cadastros/categorias/lista";
    }

    @GetMapping("/nova")
    public String novaForm(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "cadastros/categorias/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", service.buscarPorId(id));
        return "cadastros/categorias/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Categoria categoria, RedirectAttributes ra) {
        try {
            service.salvar(categoria);
            ra.addFlashAttribute("sucesso", "Categoria salva com sucesso.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return categoria.getId() == null ? "redirect:/categorias/nova" : "redirect:/categorias/" + categoria.getId() + "/editar";
        }
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Categoria excluída.");
        return "redirect:/categorias";
    }
}
