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
    public String lista(@RequestParam(required = false) Long categoriaMasterId, Model model) {
        if (categoriaMasterId != null) {
            model.addAttribute("subcategorias", service.listarSubcategoriasPorMaster(categoriaMasterId));
        } else {
            model.addAttribute("subcategorias", service.listarSubcategorias());
        }
        model.addAttribute("masters", service.listarMasters());
        model.addAttribute("filtroMasterId", categoriaMasterId);
        return "cadastros/categorias/lista";
    }

    @GetMapping("/nova")
    public String novaForm(Model model) {
        model.addAttribute("categoria", new Categoria());
        model.addAttribute("masters", service.listarMasters());
        return "cadastros/categorias/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", service.buscarPorId(id));
        model.addAttribute("masters", service.listarMasters());
        return "cadastros/categorias/form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam(required = false) Long id,
                         @RequestParam String nome,
                         @RequestParam(required = false) Integer prazoDiasUteis,
                         @RequestParam(required = false) Long categoriaPaiId,
                         RedirectAttributes ra) {
        Categoria categoria = id != null ? service.buscarPorId(id) : new Categoria();
        categoria.setNome(nome);
        categoria.setPrazoDiasUteis(prazoDiasUteis);
        categoria.setCategoriaPai(categoriaPaiId != null ? service.buscarPorId(categoriaPaiId) : null);
        try {
            service.salvar(categoria);
            ra.addFlashAttribute("sucesso", "Subcategoria salva com sucesso.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return id == null ? "redirect:/categorias/nova" : "redirect:/categorias/" + id + "/editar";
        }
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Subcategoria excluída.");
        return "redirect:/categorias";
    }
}
