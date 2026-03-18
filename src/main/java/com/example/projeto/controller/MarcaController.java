package com.example.projeto.controller;

import com.example.projeto.entity.Marca;
import com.example.projeto.service.MarcaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/marcas")
public class MarcaController {

    private final MarcaService service;

    public MarcaController(MarcaService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("marcas", service.listarTodas());
        return "cadastros/marcas/lista";
    }

    @GetMapping("/nova")
    public String novaForm(Model model) {
        model.addAttribute("marca", new Marca());
        return "cadastros/marcas/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("marca", service.buscarPorId(id));
        return "cadastros/marcas/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Marca marca, RedirectAttributes ra) {
        try {
            service.salvar(marca);
            ra.addFlashAttribute("sucesso", "Marca salva com sucesso.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return marca.getId() == null ? "redirect:/marcas/nova" : "redirect:/marcas/" + marca.getId() + "/editar";
        }
        return "redirect:/marcas";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Marca excluída.");
        return "redirect:/marcas";
    }
}
